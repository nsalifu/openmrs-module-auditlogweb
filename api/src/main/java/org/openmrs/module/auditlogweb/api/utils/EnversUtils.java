/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.auditlogweb.api.utils;

import org.openmrs.api.context.Context;

/**
 * Utility class providing helper methods related to Hibernate Envers auditing
 * and system user checks within the OpenMRS runtime context.
 */
public class EnversUtils {

    /**
     * Checks whether Hibernate Envers auditing is enabled based on runtime properties.
     *
     * @return true if Envers is enabled, false otherwise
     */
    public static boolean isEnversEnabled() {
        String value = Context.getRuntimeProperties().getProperty("hibernate.integration.envers.enabled");
        return Boolean.parseBoolean(value);
    }

    /**
     * Determines whether the currently authenticated user has the 'System Developer' role,
     * indicating administrative privileges.
     *
     * @return true if the current user is a system administrator, false otherwise
     */
    public static boolean isCurrentUserSystemAdmin() {
        return Context.isAuthenticated() && Context.getAuthenticatedUser().hasRole("System Developer");
    }
}