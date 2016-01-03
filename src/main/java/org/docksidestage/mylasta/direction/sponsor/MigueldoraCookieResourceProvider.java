/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.docksidestage.mylasta.direction.sponsor;

import org.docksidestage.mylasta.direction.MigueldoraConfig;
import org.lastaflute.core.security.InvertibleCryptographer;
import org.lastaflute.web.servlet.cookie.CookieResourceProvider;

/**
 * @author jflute
 */
public class MigueldoraCookieResourceProvider implements CookieResourceProvider {

    protected final MigueldoraConfig migueldoraConfig;
    protected final InvertibleCryptographer cookieCipher;

    public MigueldoraCookieResourceProvider(MigueldoraConfig migueldoraConfig, InvertibleCryptographer cookieCipher) {
        this.migueldoraConfig = migueldoraConfig;
        this.cookieCipher = cookieCipher;
    }

    public String provideDefaultPath() {
        return migueldoraConfig.getCookieDefaultPath();
    }

    public Integer provideDefaultExpire() {
        return migueldoraConfig.getCookieDefaultExpireAsInteger();
    }

    public InvertibleCryptographer provideCipher() {
        return cookieCipher;
    }
}
