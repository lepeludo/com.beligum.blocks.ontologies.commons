/*
 * Copyright 2017 Republic of Reinvention bvba. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beligum.blocks.ontologies.commons.config;

import com.beligum.base.server.R;
import com.beligum.base.utils.Logger;

/**
 * Created by bram on 12.06.17.
 */
public class Settings
{
    //-----CONSTANTS-----
    private static final String KEY_PREFIX = "blocks.ontologies.commons";
    private static final String GEONAMES_USERNAME_KEY = KEY_PREFIX + ".geonames.username";
    private static final String DEFAULT_GEONAMES_USERNAME = "demo";

    //-----VARIABLES-----
    private static Settings instance;
    private String cachedGeonamesUsername;

    //-----CONSTRUCTORS-----
    private Settings()
    {
    }

    //-----PUBLIC METHODS-----
    public static Settings instance()
    {
        if (Settings.instance == null) {
            Settings.instance = new Settings();
        }
        return Settings.instance;
    }

    public String getGeonamesUsername()
    {
        if (this.cachedGeonamesUsername == null) {
            this.cachedGeonamesUsername = R.configuration().getString(GEONAMES_USERNAME_KEY, null);
            if (this.cachedGeonamesUsername == null) {
                Logger.warn("No geonames username specified, using default username '" + DEFAULT_GEONAMES_USERNAME + "', but this is not optimal...");
                this.cachedGeonamesUsername = DEFAULT_GEONAMES_USERNAME;
            }
        }

        return this.cachedGeonamesUsername;
    }

    //-----PRIVATE METHODS-----
}
