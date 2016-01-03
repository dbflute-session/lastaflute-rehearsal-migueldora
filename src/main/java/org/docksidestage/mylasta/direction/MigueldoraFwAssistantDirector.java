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
package org.docksidestage.mylasta.direction;

import javax.annotation.Resource;

import org.docksidestage.mylasta.direction.sponsor.MigueldoraActionAdjustmentProvider;
import org.docksidestage.mylasta.direction.sponsor.MigueldoraApiFailureHook;
import org.docksidestage.mylasta.direction.sponsor.MigueldoraCookieResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.MigueldoraCurtainBeforeHook;
import org.docksidestage.mylasta.direction.sponsor.MigueldoraJsonResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.MigueldoraListedClassificationProvider;
import org.docksidestage.mylasta.direction.sponsor.MigueldoraMailDeliveryDepartmentCreator;
import org.docksidestage.mylasta.direction.sponsor.MigueldoraSecurityResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.MigueldoraTimeResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.MigueldoraUserLocaleProcessProvider;
import org.docksidestage.mylasta.direction.sponsor.MigueldoraUserTimeZoneProcessProvider;
import org.lastaflute.core.direction.CachedFwAssistantDirector;
import org.lastaflute.core.direction.FwAssistDirection;
import org.lastaflute.core.direction.FwCoreDirection;
import org.lastaflute.core.security.InvertibleCryptographer;
import org.lastaflute.core.security.OneWayCryptographer;
import org.lastaflute.db.dbflute.classification.ListedClassificationProvider;
import org.lastaflute.db.direction.FwDbDirection;
import org.lastaflute.mixer2.Mixer2RenderingProvider;
import org.lastaflute.thymeleaf.ThymeleafRenderingProvider;
import org.lastaflute.web.direction.FwWebDirection;
import org.lastaflute.web.response.HtmlResponse;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.ruts.renderer.HtmlRenderingProvider;

/**
 * @author jflute
 */
public class MigueldoraFwAssistantDirector extends CachedFwAssistantDirector {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private MigueldoraConfig migueldoraConfig;

    // ===================================================================================
    //                                                                              Assist
    //                                                                              ======
    @Override
    protected void prepareAssistDirection(FwAssistDirection direction) {
        direction.directConfig(nameList -> nameList.add("migueldora_config.properties"), "migueldora_env.properties");
    }

    // ===================================================================================
    //                                                                               Core
    //                                                                              ======
    @Override
    protected void prepareCoreDirection(FwCoreDirection direction) {
        // this configuration is on migueldora_env.properties because this is true only when development
        direction.directDevelopmentHere(migueldoraConfig.isDevelopmentHere());

        // titles of the application for logging are from configurations
        direction.directLoggingTitle(migueldoraConfig.getDomainTitle(), migueldoraConfig.getEnvironmentTitle());

        // this configuration is on sea_env.properties because it has no influence to production
        // even if you set true manually and forget to set false back
        direction.directFrameworkDebug(migueldoraConfig.isFrameworkDebug()); // basically false

        // you can add your own process when your application is booting
        direction.directCurtainBefore(createCurtainBeforeHook());

        direction.directSecurity(createSecurityResourceProvider());
        direction.directTime(createTimeResourceProvider());
        direction.directJson(createJsonResourceProvider());
        direction.directMail(createMailDeliveryDepartmentCreator().create());
    }

    protected MigueldoraCurtainBeforeHook createCurtainBeforeHook() {
        return new MigueldoraCurtainBeforeHook();
    }

    protected MigueldoraSecurityResourceProvider createSecurityResourceProvider() { // #change_it_first
        final InvertibleCryptographer inver = InvertibleCryptographer.createAesCipher("harbor:dockside");
        final OneWayCryptographer oneWay = OneWayCryptographer.createSha256Cryptographer();
        return new MigueldoraSecurityResourceProvider(inver, oneWay);
    }

    protected MigueldoraTimeResourceProvider createTimeResourceProvider() {
        return new MigueldoraTimeResourceProvider(migueldoraConfig);
    }

    protected MigueldoraJsonResourceProvider createJsonResourceProvider() {
        return new MigueldoraJsonResourceProvider();
    }

    protected MigueldoraMailDeliveryDepartmentCreator createMailDeliveryDepartmentCreator() {
        return new MigueldoraMailDeliveryDepartmentCreator(migueldoraConfig);
    }

    // ===================================================================================
    //                                                                                 DB
    //                                                                                ====
    @Override
    protected void prepareDbDirection(FwDbDirection direction) {
        direction.directClassification(createListedClassificationProvider());
    }

    protected ListedClassificationProvider createListedClassificationProvider() {
        return new MigueldoraListedClassificationProvider();
    }

    // ===================================================================================
    //                                                                                Web
    //                                                                               =====
    @Override
    protected void prepareWebDirection(FwWebDirection direction) {
        direction.directRequest(createUserLocaleProcessProvider(), createUserTimeZoneProcessProvider());
        direction.directCookie(createCookieResourceProvider());
        direction.directAdjustment(createActionAdjustmentProvider());
        direction.directMessage(nameList -> nameList.add("migueldora_message"), "migueldora_label");
        direction.directApiCall(createApiFailureHook());
        direction.directHtmlRendering(createHtmlRenderingProvider());
    }

    protected MigueldoraUserLocaleProcessProvider createUserLocaleProcessProvider() {
        return new MigueldoraUserLocaleProcessProvider();
    }

    protected MigueldoraUserTimeZoneProcessProvider createUserTimeZoneProcessProvider() {
        return new MigueldoraUserTimeZoneProcessProvider();
    }

    protected MigueldoraCookieResourceProvider createCookieResourceProvider() { // #change_it_first
        final InvertibleCryptographer cr = InvertibleCryptographer.createAesCipher("dockside:harbor");
        return new MigueldoraCookieResourceProvider(migueldoraConfig, cr);
    }

    protected MigueldoraActionAdjustmentProvider createActionAdjustmentProvider() {
        return new MigueldoraActionAdjustmentProvider();
    }

    protected MigueldoraApiFailureHook createApiFailureHook() {
        return new MigueldoraApiFailureHook();
    }

    protected HtmlRenderingProvider createHtmlRenderingProvider() {
        final ThymeleafRenderingProvider thymeleaf = createThymeleafRenderingProvider();
        final Mixer2RenderingProvider mixer2 = createMixer2RenderingProvider();
        return new HtmlRenderingProvider() {

            @Override
            public HtmlRenderer provideRenderer(ActionRuntime runtime, NextJourney journey) {
                return chooseProvider(runtime, journey).provideRenderer(runtime, journey);
            }

            @Override
            public HtmlResponse provideShowErrorsResponse(ActionRuntime runtime) {
                return mixer2.provideShowErrorsResponse(runtime);
            }

            private HtmlRenderingProvider chooseProvider(ActionRuntime runtime, NextJourney journey) {
                return journey.getViewObject().isPresent() ? mixer2 : thymeleaf;
            }
        };
    }

    protected ThymeleafRenderingProvider createThymeleafRenderingProvider() { // will be deleted
        return new ThymeleafRenderingProvider().asDevelopment(migueldoraConfig.isDevelopmentHere());
    }

    protected Mixer2RenderingProvider createMixer2RenderingProvider() {
        return new Mixer2RenderingProvider().asDevelopment(migueldoraConfig.isDevelopmentHere());
    }
}
