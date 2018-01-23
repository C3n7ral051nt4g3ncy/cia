/*
 * Copyright 2010 James Pether Sörling
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
package com.hack23.cia.service.impl.action.application.access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hack23.cia.model.internal.application.system.impl.ApplicationActionEvent;
import com.hack23.cia.model.internal.application.system.impl.ApplicationActionEvent_;
import com.hack23.cia.model.internal.application.system.impl.ApplicationConfiguration;
import com.hack23.cia.model.internal.application.system.impl.ApplicationOperationType;
import com.hack23.cia.model.internal.application.system.impl.ApplicationSession;
import com.hack23.cia.model.internal.application.system.impl.ApplicationSession_;
import com.hack23.cia.model.internal.application.system.impl.ConfigurationGroup;
import com.hack23.cia.model.internal.application.user.impl.UserAccount;
import com.hack23.cia.model.internal.application.user.impl.UserAccount_;
import com.hack23.cia.service.api.action.common.ServiceResponse.ServiceResult;
import com.hack23.cia.service.data.api.ApplicationActionEventDAO;
import com.hack23.cia.service.data.api.ApplicationConfigurationService;
import com.hack23.cia.service.data.api.ApplicationSessionDAO;
import com.hack23.cia.service.data.api.UserDAO;

/**
 * The Class LoginBlockedAccessImpl.
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class LoginBlockedAccessImpl implements LoginBlockedAccess {

	private static final int DEFAULT_MAX_LOGINS = 5;

	private static final int DEFAULT_MAX_LOGINS_BY_IP = 10;

	private static final int ONE_HOUR = 3600 * 1000;

	/** The Constant DEFAULT_MAX_LOGIN_ATTEMPTS. */
	private static final String DEFAULT_MAX_LOGIN_ATTEMPTS = "5";

	/**
	 * The Constant APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_IP.
	 */
	private static final String APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_IP = "application.authentication.allow.max.recent.failed.logins.by.ip";

	/**
	 * The Constant
	 * APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_SESSION.
	 */
	private static final String APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_SESSION = "application.authentication.allow.max.recent.failed.logins.by.session";

	/** The Constant BLOCKS_LOGIN_ATTEMPTS. */
	private static final String BLOCKS_LOGIN_ATTEMPTS = "Blocks login attempts";

	/** The Constant LOGIN_BLOCKER. */
	private static final String LOGIN_BLOCKER = "LoginBlocker";

	/** The Constant MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_IP. */
	private static final String MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_IP = "Max failed login attempts recent hour per ip";

	/** The Constant MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_SESSION. */
	private static final String MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_SESSION = "Max failed login attempts recent hour per session";

	/** The Constant MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_USER. */
	private static final String MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_USER = "Max failed login attempts recent hour per user";

	/**
	 * The Constant
	 * APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_USER.
	 */
	private static final String APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_USER = "application.authentication.allow.max.recent.failed.logins.by.user";

	/** The Constant BLOCKS_ANY_LOGIN_ATTEMPTS_AFTER_THIS_NUMBER_IS_REACHED. */
	private static final String BLOCKS_ANY_LOGIN_ATTEMPTS_AFTER_THIS_NUMBER_IS_REACHED = "Blocks any login attempts after this number is reached";

	/** The Constant LOGIN_BLOCK_SETTINGS. */
	private static final String LOGIN_BLOCK_SETTINGS = "LoginBlock settings:{}";

	/** The Constant BLOCKED_BY_MORE_THAN_5_RECENT_LOGIN_ATTEMPTS_BY_THIS_IP. */
	private static final String BLOCKED_BY_MORE_THAN_5_RECENT_LOGIN_ATTEMPTS_BY_THIS_IP = "Blocked by more than 5 recent login attempts by this ip";

	/** The Constant BLOCKED_BY_MORE_THAN_5_LOGIN_ATTEMPTS_BY_THIS_SESSION. */
	private static final String BLOCKED_BY_MORE_THAN_5_LOGIN_ATTEMPTS_BY_THIS_SESSION = "Blocked by more than 5 login attempts by this session";

	/** The Constant BLOCKED_BY_MORE_THAN_5_RECENT_LOGIN_ATTEMPTS_BY_THIS_USER. */
	private static final String BLOCKED_BY_MORE_THAN_5_RECENT_LOGIN_ATTEMPTS_BY_THIS_USER = "Blocked by more than 5 recent login attempts by this user";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(LoginBlockedAccessImpl.class);

	/** The user DAO. */
	@Autowired
	private UserDAO userDAO;

	/** The application session DAO. */
	@Autowired
	private ApplicationSessionDAO applicationSessionDAO;

	/** The application action event DAO. */
	@Autowired
	private ApplicationActionEventDAO applicationActionEventDAO;

	/** The application configuration service. */
	@Autowired
	private ApplicationConfigurationService applicationConfigurationService;

	/**
	 * Inits the settings.
	 */
	@PostConstruct
	public void initSettings() {
		LOGGER.info(LOGIN_BLOCK_SETTINGS,applicationConfigurationService.checkValueOrLoadDefault(MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_USER, BLOCKS_ANY_LOGIN_ATTEMPTS_AFTER_THIS_NUMBER_IS_REACHED, ConfigurationGroup.AUTHENTICATION, LoginBlockedAccessImpl.class.getSimpleName(), LOGIN_BLOCKER, BLOCKS_LOGIN_ATTEMPTS, APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_USER, DEFAULT_MAX_LOGIN_ATTEMPTS));
		LOGGER.info(LOGIN_BLOCK_SETTINGS,applicationConfigurationService.checkValueOrLoadDefault(MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_SESSION, BLOCKS_ANY_LOGIN_ATTEMPTS_AFTER_THIS_NUMBER_IS_REACHED, ConfigurationGroup.AUTHENTICATION, LoginBlockedAccessImpl.class.getSimpleName(), LOGIN_BLOCKER, BLOCKS_LOGIN_ATTEMPTS, APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_SESSION,DEFAULT_MAX_LOGIN_ATTEMPTS));
		LOGGER.info(LOGIN_BLOCK_SETTINGS,applicationConfigurationService.checkValueOrLoadDefault(MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_IP, BLOCKS_ANY_LOGIN_ATTEMPTS_AFTER_THIS_NUMBER_IS_REACHED, ConfigurationGroup.AUTHENTICATION, LoginBlockedAccessImpl.class.getSimpleName(), LOGIN_BLOCKER, BLOCKS_LOGIN_ATTEMPTS, APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_IP, DEFAULT_MAX_LOGIN_ATTEMPTS));
	}


	@Override
	public LoginBlockResult isBlocked(final String sessionId, final String email) {
		final LoginBlockResultImpl loginBlockResultImpl = new LoginBlockResultImpl();
		blockByUserFailedLoginAttempts(email, loginBlockResultImpl);
		blockBySessionOrIpFailedLoginAttempts(sessionId, loginBlockResultImpl);
		return loginBlockResultImpl;
	}

	/**
	 * Block by user failed login attempts.
	 *
	 * @param email
	 *            the email
	 * @param loginBlockResultImpl
	 *            the login block result impl
	 */
	private void blockByUserFailedLoginAttempts(final String email, final LoginBlockResultImpl loginBlockResultImpl) {
		final UserAccount userExist = userDAO.findFirstByProperty(UserAccount_.email, email);

		if (userExist != null) {

			final ApplicationConfiguration maxLoginAttemptsByUser = applicationConfigurationService.checkValueOrLoadDefault(MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_USER, BLOCKS_ANY_LOGIN_ATTEMPTS_AFTER_THIS_NUMBER_IS_REACHED, ConfigurationGroup.AUTHENTICATION, LoginBlockedAccessImpl.class.getSimpleName(), LOGIN_BLOCKER, BLOCKS_LOGIN_ATTEMPTS, APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_USER, DEFAULT_MAX_LOGIN_ATTEMPTS);

			final List<ApplicationActionEvent> failedLoginsByThisUser = applicationActionEventDAO.findListByProperty(
					new Object[] { email, ApplicationOperationType.AUTHENTICATION, ServiceResult.FAILURE.toString() },
					ApplicationActionEvent_.elementId, ApplicationActionEvent_.applicationOperation,
					ApplicationActionEvent_.applicationMessage);

			final Date oneHourAgo = new Date(System.currentTimeMillis() - ONE_HOUR);
			final Map<Boolean, List<ApplicationActionEvent>> recentOldLoginAttemptsMap = failedLoginsByThisUser.stream()
					.collect(Collectors.groupingBy(x -> x.getCreatedDate().after(oneHourAgo)));
			final List<ApplicationActionEvent> recentFailedLogins = recentOldLoginAttemptsMap.get(Boolean.TRUE);
			if (recentFailedLogins != null && recentFailedLogins.size() > NumberUtils.toInt(maxLoginAttemptsByUser.getPropertyValue(),DEFAULT_MAX_LOGINS)) {
				loginBlockResultImpl.setBlocked(true);
				loginBlockResultImpl.addMessages(BLOCKED_BY_MORE_THAN_5_RECENT_LOGIN_ATTEMPTS_BY_THIS_USER);
			}
		}
	}

	/**
	 * Block by session or ip failed login attempts.
	 *
	 * @param sessionId
	 *            the session id
	 * @param loginBlockResultImpl
	 *            the login block result impl
	 */
	private void blockBySessionOrIpFailedLoginAttempts(final String sessionId, final LoginBlockResultImpl loginBlockResultImpl) {
		final ApplicationSession applicationSession = applicationSessionDAO
				.findFirstByProperty(ApplicationSession_.sessionId, sessionId);

		if (applicationSession != null) {

			final ApplicationConfiguration maxLoginAttemptsBySession = applicationConfigurationService.checkValueOrLoadDefault(MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_SESSION, BLOCKS_ANY_LOGIN_ATTEMPTS_AFTER_THIS_NUMBER_IS_REACHED, ConfigurationGroup.AUTHENTICATION, LoginBlockedAccessImpl.class.getSimpleName(), LOGIN_BLOCKER, BLOCKS_LOGIN_ATTEMPTS, APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_SESSION,DEFAULT_MAX_LOGIN_ATTEMPTS);
			final List<ApplicationActionEvent> failedLoginsByThisSession = applicationActionEventDAO.findListByProperty(
					new Object[] { sessionId, ApplicationOperationType.AUTHENTICATION,
							ServiceResult.FAILURE.toString() },
					ApplicationActionEvent_.sessionId, ApplicationActionEvent_.applicationOperation,
					ApplicationActionEvent_.applicationMessage);
			if (failedLoginsByThisSession.size() > NumberUtils.toInt(maxLoginAttemptsBySession.getPropertyValue(),DEFAULT_MAX_LOGINS)) {
				loginBlockResultImpl.setBlocked(true);
				loginBlockResultImpl.addMessages(BLOCKED_BY_MORE_THAN_5_LOGIN_ATTEMPTS_BY_THIS_SESSION);
			}

			if (!("0:0:0:0:0:0:0:1".equals(applicationSession.getIpInformation()) || "127.0.0.1".equals(applicationSession.getIpInformation()))) {

				final List<ApplicationSession> applicationSessionsByIp = applicationSessionDAO
						.findListByProperty(ApplicationSession_.ipInformation, applicationSession.getIpInformation());

				final List<String> sessionIdsWithIp = applicationSessionsByIp.stream().map(ApplicationSession::getSessionId)
						.collect(Collectors.toList());

				final List<ApplicationActionEvent> applicationEventsWithIp = applicationActionEventDAO
						.findListByPropertyInList(ApplicationActionEvent_.sessionId,
								sessionIdsWithIp.toArray(new Object[sessionIdsWithIp.size()]));

				final Date oneHourAgo = new Date(System.currentTimeMillis() - ONE_HOUR);
				final Map<Boolean, List<ApplicationActionEvent>> recentOldLoginAttemptsMap = applicationEventsWithIp
						.stream()
						.filter(x -> x.getApplicationOperation() == ApplicationOperationType.AUTHENTICATION
								&& x.getApplicationMessage().equals(ServiceResult.FAILURE.toString()))
						.collect(Collectors.groupingBy(x -> x.getCreatedDate().after(oneHourAgo)));
				final List<ApplicationActionEvent> recentFailedLogins = recentOldLoginAttemptsMap
						.get(Boolean.TRUE);

				final ApplicationConfiguration maxLoginAttemptsByIp = applicationConfigurationService.checkValueOrLoadDefault(MAX_FAILED_LOGIN_ATTEMPTS_RECENT_HOUR_PER_IP, BLOCKS_ANY_LOGIN_ATTEMPTS_AFTER_THIS_NUMBER_IS_REACHED, ConfigurationGroup.AUTHENTICATION, LoginBlockedAccessImpl.class.getSimpleName(), LOGIN_BLOCKER, BLOCKS_LOGIN_ATTEMPTS, APPLICATION_AUTHENTICATION_ALLOW_MAX_RECENT_FAILED_LOGINS_BY_IP, DEFAULT_MAX_LOGIN_ATTEMPTS);
				if (recentFailedLogins != null && recentFailedLogins.size() > NumberUtils.toInt(maxLoginAttemptsByIp.getPropertyValue(),DEFAULT_MAX_LOGINS_BY_IP)) {
					loginBlockResultImpl.setBlocked(true);
					loginBlockResultImpl.addMessages(BLOCKED_BY_MORE_THAN_5_RECENT_LOGIN_ATTEMPTS_BY_THIS_IP);
				}
			}
		}
	}

	/**
	 * The Class LoginBlockResultImpl.
	 */
	static final class LoginBlockResultImpl implements LoginBlockResult {

		/** The is blocked. */
		private boolean isBlocked;

		/** The messages. */
		private final List<String> messages = new ArrayList<>();

		@Override
		public boolean isBlocked() {
			return isBlocked;
		}

		/**
		 * Adds the messages.
		 *
		 * @param msg
		 *            the msg
		 */
		public void addMessages(final String msg) {
			this.messages.add(msg);
		}

		/**
		 * Sets the blocked.
		 *
		 * @param isBlocked
		 *            the new blocked
		 */
		public void setBlocked(boolean isBlocked) {
			this.isBlocked = isBlocked;
		}

		@Override
		public List<String> getMessages() {
			return Collections.unmodifiableList(messages);
		}

	}

}
