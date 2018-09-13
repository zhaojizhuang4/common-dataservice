/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */
package org.acumos.cds.migrate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.acumos.cds.client.CommonDataServiceRestClientImpl;
import org.acumos.cds.client.ICommonDataServiceRestClient;
import org.acumos.cds.domain.MLPDocument;
import org.acumos.cds.domain.MLPRevisionDescription;
import org.acumos.cds.domain.MLPSolution;
import org.acumos.cds.domain.MLPSolutionRevision;
import org.acumos.cds.migrate.client.CMSReaderClient;
import org.acumos.cds.migrate.client.CMSWorkspace;
import org.acumos.cds.migrate.domain.CMSNameList;
import org.acumos.cds.migrate.domain.CMSRevisionDescription;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.cds.transport.RestPageResponse;
import org.acumos.nexus.client.NexusArtifactClient;
import org.acumos.nexus.client.RepositoryLocation;
import org.acumos.nexus.client.data.UploadArtifactInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Migrates the following data from CMS to CDS and Nexus:
 * <OL>
 * <LI>Solution image, migrates to CDS
 * <LI>Revision descriptions, org and public are separate, migrates to CDS
 * <LI>Revision documents, org and public are separate, metadata to CDS and
 * content to Nexus.
 * </OL>
 * Configured by properties file in current directory.
 * 
 * Nexus storage hierarchy: prefix from properties, then solution id, then
 * revision id; use access type code as the version.
 */
public class MigrateCmsToCdsApp {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// I think this will grow
	private static final String specialCharRegex = "[!@#$%^&*()<>{}|:?+=.\\s]+";
	/**
	 * Migrates data.
	 * 
	 * @param args
	 *            Ignored
	 */
	public static void main(String[] args) {

		// Report stats at end
		long solCount = 0L, revCount = 0L;
		int migrPicSucc = 0, migrPicFail = 0;
		int migrDescSucc = 0, migrDescFail = 0;
		int migrDocSucc = 0, migrDocFail = 0;

		MigrateProperties props = null;
		try {
			props = new MigrateProperties();
		} catch (Exception ex) {
			logger.error("Failed to read properties, stopping.");
		}

		final String cdsUrl = props.getProperty(MigrateProperties.CDS_URL);
		final String cmsUrl = props.getProperty(MigrateProperties.CMS_URL);
		final String nexusUrl = props.getProperty(MigrateProperties.NEXUS_URL);
		final String nexusPrefix = props.getProperty(MigrateProperties.NEXUS_PREFIX);
		logger.info("Migrate data FROM CMS {}", cmsUrl);
		logger.info("Migrate data TO CDS {}", cdsUrl);
		logger.info("Migrate data TO Nexus {} using prefix {}", nexusUrl, nexusPrefix);

		ICommonDataServiceRestClient cdsClient = CommonDataServiceRestClientImpl.getInstance(cdsUrl,
				props.getProperty(MigrateProperties.CDS_USER), props.getProperty(MigrateProperties.CDS_PASS));
		CMSReaderClient cmsClient = new CMSReaderClient(cmsUrl, props.getProperty(MigrateProperties.CMS_USER),
				props.getProperty(MigrateProperties.CMS_PASS));
		// Is "1" a magic ID? Don't use a proxy here
		RepositoryLocation repoLoc = new RepositoryLocation("1", nexusUrl,
				props.getProperty(MigrateProperties.NEXUS_USER), props.getProperty(MigrateProperties.NEXUS_PASS), null);
		NexusArtifactClient nexusClient = new NexusArtifactClient(repoLoc);

		// Validate CDS connection
		try {
			solCount = cdsClient.getSolutionCount();
			logger.info("Total solution count {}", solCount);
		} catch (HttpStatusCodeException ex) {
			logger.error("Failed to get count of solution records in CDS: {}", ex.getResponseBodyAsString());
			return;
		}

		final int pageSize = 100;
		for (int page = 0;; ++page) {
			RestPageRequest request = new RestPageRequest(page, pageSize);
			RestPageResponse<MLPSolution> sols = cdsClient.getSolutions(request);
			logger.info("Processing solution page {} with {} elements", page, sols.getNumberOfElements());

			for (MLPSolution s : sols.getContent()) {
				logger.debug("Checking solution ID {} name {}", s.getSolutionId(), s.getName());

				// Item 1: solution image
				CMSNameList solNames = cmsClient.getSolutionImageName(s.getSolutionId());
				if (solNames.getResponse_body().isEmpty()) {
					logger.info("Solution {} has no CMS image", s.getSolutionId());
				} else {
					String imgName = solNames.getResponse_body().get(0);
					if (s.getPicture() != null) {
						// CDS already has it
						logger.info("Solution {} image {} already migrated", s.getSolutionId(), imgName);
					} else {
						logger.info("Migrating solution {} image: {}", s.getSolutionId(), imgName);
						byte[] cmsImage = cmsClient.getSolutionImage(s.getSolutionId(), imgName);
						s.setPicture(cmsImage);
						try {
							cdsClient.updateSolution(s);
							++migrPicSucc;
						} catch (HttpStatusCodeException ex) {
							++migrPicFail;
							logger.error("Failed to update solution {} with image: {}", s.getSolutionId(),
									ex.getResponseBodyAsString());
						}
					}
				}

				List<MLPSolutionRevision> revs = cdsClient.getSolutionRevisions(s.getSolutionId());
				for (MLPSolutionRevision r : revs) {
					++revCount;
					for (CMSWorkspace ws : CMSWorkspace.values()) {
						logger.debug("Checking revision {} CMS workspace {}", r.getRevisionId(), ws.getCmsKey());

						// Item 2: revision descriptions, 0..1 per workspace value
						CMSRevisionDescription cmsRevDesc = null;
						try {
							cmsRevDesc = cmsClient.getRevisionDescription(s.getSolutionId(), r.getRevisionId(),
									ws.getCmsKey());
						} catch (Exception ex) {
							// Should never happen; CMS answers 200 even when it has no data
							logger.error("Failed to get description for revision {} access {}", r.getRevisionId(),
									ws.getCmsKey());
						}
						if (cmsRevDesc == null || cmsRevDesc.getDescription() == null
								|| cmsRevDesc.getDescription().isEmpty()) {
							logger.info("Revision {} access {} has no description", r.getRevisionId(), ws.getCmsKey());
						} else {
							MLPRevisionDescription cdsRevDesc = null;
							try {
								cdsRevDesc = cdsClient.getRevisionDescription(r.getRevisionId(), ws.getCdsKey());
								logger.info("Revision {} access {} description already migrated", r.getRevisionId(),
										ws.getCmsKey());
							} catch (HttpStatusCodeException ex) {
								// CDS returned an error which means it does not exist
								logger.info("Creating revision {} access {} description", r.getRevisionId(),
										ws.getCmsKey());
								cdsRevDesc = new MLPRevisionDescription(r.getRevisionId(), ws.getCdsKey(),
										cmsRevDesc.getDescription());
								try {
									cdsClient.createRevisionDescription(cdsRevDesc);
									++migrDescSucc;
								} catch (HttpStatusCodeException ex2) {
									// Should never happen
									logger.error("Failed to create revision description at CDS", ex2);
									++migrDescFail;
								}
							}
						}

						// Item 3: revision documents, 0..many per workspace value
						CMSNameList cmsRevDocs = cmsClient.getRevisionDocumentNames(s.getSolutionId(),
								r.getRevisionId(), ws.getCmsKey());
						if (cmsRevDocs.getResponse_body().isEmpty()) {
							logger.info("Revision {} access {} has no documents", r.getRevisionId(), ws.getCmsKey());
						} else {
							List<MLPDocument> cdsRevDocs = cdsClient.getSolutionRevisionDocuments(r.getRevisionId(),
									ws.getCdsKey());
							for (String cmsDocName : cmsRevDocs.getResponse_body()) {
								final String[] cmsDocParts = splitFileBaseExt(cmsDocName);
								// Give up if no suffix; don't want to guess
								if (cmsDocParts.length != 2) {
									logger.error(
											"No suffix available for packaging; skipping revision {} access {} document {}",
											r.getRevisionId(), ws.getCmsKey(), cmsDocName);
									++migrDocFail;
								} else {
									// Produce a clean basename for Nexus by replacing special characters
									final String nexusDocBase = cmsDocParts[0].replaceAll(specialCharRegex, "-");	
									final String nexusDocSuffix = cmsDocParts[1];
									final String nexusDocName = nexusDocBase + "." + nexusDocSuffix;
									if (findDocNameInCdsList(nexusDocName, cdsRevDocs)) {
										// CDS has already
										logger.info("Revision {} access {} document {} already migrated",
												r.getAccessTypeCode(), ws.getCmsKey(), nexusDocName);
									} else {
										final String groupId = createNexusGroupId(nexusPrefix, s.getSolutionId(),
												r.getRevisionId());
										byte[] cmsDoc = null;
										try {
											cmsDoc = cmsClient.getRevisionDocument(s.getSolutionId(), r.getRevisionId(),
													ws.getCmsKey(), cmsDocName);
										} catch (Exception ex) {
											logger.error("Failed to get CMS revision {} document {}; exception follows",
													r.getRevisionId(), cmsDocName);
											logger.error("Exception in fetch", ex);
											++migrDocFail;
										}
										if (cmsDoc != null) {
											InputStream inputStream = new ByteArrayInputStream(cmsDoc);
											UploadArtifactInfo uploadInfo = null;
											logger.info(
													"Uploading revision {} access {} document {}.{} to Nexus group {}",
													r.getRevisionId(), ws.getCmsKey(), nexusDocBase, nexusDocSuffix,
													groupId);
											try {
												uploadInfo = nexusClient.uploadArtifact(groupId, nexusDocBase,
														ws.getCdsKey(), nexusDocSuffix, cmsDoc.length, inputStream);
											} catch (Exception ex) {
												logger.error(
														"Failed to upload revision {} doc base {} doc suffix as nexus artifact; exception follows",
														r.getRevisionId(), nexusDocBase,  nexusDocSuffix);
												logger.error("Exception in upload", ex);
												++migrDocFail;
											}
											if (uploadInfo != null) {
												MLPDocument cdsDoc = new MLPDocument();
												cdsDoc.setName(nexusDocName);
												cdsDoc.setUri(uploadInfo.getArtifactMvnPath());
												cdsDoc.setSize(cmsDoc.length);
												cdsDoc.setUserId(r.getUserId());
												logger.info("Creating revision {} access {} document {} metadata",
														r.getRevisionId(), ws.getCmsKey(), nexusDocName);
												try {
													cdsDoc = cdsClient.createDocument(cdsDoc);
													cdsClient.addSolutionRevisionDocument(r.getRevisionId(),
															ws.getCdsKey(), cdsDoc.getDocumentId());
													++migrDocSucc;
												} catch (HttpStatusCodeException ex) {
													logger.error(
															"Failed to create revision {} document {}; server response {}",
															r.getRevisionId(), nexusDocName,
															ex.getResponseBodyAsString());
													++migrDocFail;
												}
											}
										}
									}
								}

							} // for doc

						} // list not empty

					} // for workspace

				} // for revision

			} // for solution in page

			// Stop after the last page
			if (sols.getNumberOfElements() < pageSize)
				break;

		} // for page

		logger.info("Migration statistics:");
		logger.info("Solutions checked: {}", solCount);
		logger.info("Revisions checked: {}", revCount);
		logger.info("Pictures migrated: {} success, {} fail", migrPicSucc, migrPicFail);
		logger.info("Descriptions migrated: {} success, {} fail", migrDescSucc, migrDescFail);
		logger.info("Documents migrated: {} success, {} fail", migrDocSucc, migrDocFail);
	}

	/**
	 * Factors code of loop above
	 * 
	 * @param name
	 *            Name of document
	 * @param docs
	 *            List of documents
	 * @return True if doc with specified name occurs in list
	 */
	private static boolean findDocNameInCdsList(String name, List<MLPDocument> docs) {
		for (MLPDocument d : docs)
			if (name.equals(d.getName()))
				return true;
		return false;
	}

	/**
	 * Forms Nexus group id as prefix.SolutionId.RevisionId
	 * 
	 * @param prefix
	 *            Nexus prefix string
	 * @param solutionId
	 *            Solution ID
	 * @param revisionId
	 *            Revision ID
	 * @return Dotted string suitable for use as Nexus group id
	 */
	private static String createNexusGroupId(String prefix, String solutionId, String revisionId) {
		if (prefix.endsWith("."))
			throw new IllegalArgumentException("Malformed prefix, must not end in period: " + prefix);
		return String.join(".", prefix, solutionId, revisionId);
	}

	/**
	 * Splits name into basename and extension at the last period.
	 * 
	 * @param name
	 *            containing a period
	 * @return Array of size 2; empty if the name has no period
	 */
	private static String[] splitFileBaseExt(String name) {
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf < 0 || lastIndexOf + 1 == name.length())
			return new String[0]; // empty extension is bogus
		return new String[] { name.substring(0, lastIndexOf), name.substring(lastIndexOf + 1) };
	}

}
