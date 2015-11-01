/*
 * Copyright 2014 James Pether Sörling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *	$Id$
 *  $HeadURL$
*/
package com.hack23.cia.web.impl.ui.application.views.common.pagelinks;

import com.hack23.cia.model.external.riksdagen.person.impl.PersonData;
import com.hack23.cia.model.internal.application.data.committee.impl.ViewRiksdagenCommittee;
import com.hack23.cia.model.internal.application.data.ministry.impl.ViewRiksdagenMinistry;
import com.hack23.cia.model.internal.application.data.party.impl.ViewRiksdagenParty;
import com.vaadin.ui.Link;

public interface PageLinkFactory {

	/**
	 * Creates a new PageLink object.
	 *
	 * @return the link
	 */
	Link createMainViewPageLink();

	/**
	 * Creates a new PageLink object.
	 *
	 * @return the link
	 */
	Link createTestChartViewPageLink();

	/**
	 * Creates a new PageLink object.
	 *
	 * @return the link
	 */
	Link createMinistryRankingViewPageLink();

	/**
	 * Creates a new PageLink object.
	 *
	 * @return the link
	 */
	Link createCommitteeRankingViewPageLink();

	/**
	 * Creates a new PageLink object.
	 *
	 * @return the link
	 */
	Link createPartyRankingViewPageLink();

	/**
	 * Creates a new PageLink object.
	 *
	 * @return the link
	 */
	Link createPoliticianRankingViewPageLink();

	/**
	 * Creates a new PageLink object.
	 *
	 * @return the link
	 */
	Link createAdminDataSummaryViewPageLink();

	/**
	 * Creates a new PageLink object.
	 *
	 * @return the link
	 */
	Link createAdminAgentOperationViewPageLink();

	/**
	 * Adds the committee page link.
	 *
	 * @param data
	 *            the data
	 * @return the link
	 */
	Link addCommitteePageLink(ViewRiksdagenCommittee data);

	/**
	 * Adds the ministry page link.
	 *
	 * @param data
	 *            the data
	 * @return the link
	 */
	Link addMinistryPageLink(ViewRiksdagenMinistry data);

	/**
	 * Adds the party page link.
	 *
	 * @param data
	 *            the data
	 * @return the link
	 */
	Link addPartyPageLink(ViewRiksdagenParty data);

	/**
	 * Creates a new PageLink object.
	 *
	 * @param personData
	 *            the person data
	 * @return the link
	 */
	Link createPoliticianPageLink(PersonData personData);

}
