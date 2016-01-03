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
package org.docksidestage.app.web.signin;

import org.docksidestage.app.web.base.MigueldoraBaseView;
import org.lastaflute.mixer2.view.Mixer2Supporter;
import org.mixer2.jaxb.xhtml.Body;
import org.mixer2.jaxb.xhtml.Html;

/**
 * @author jflute
 */
public class SigninView extends MigueldoraBaseView {

    private final SigninForm form;

    public SigninView(SigninForm form) {
        this.form = form;
    }

    @Override
    protected void render(Html html, Mixer2Supporter supporter) {
        Body body = html.getBody();
        if (isNotEmpty(form.account)) { // #pending supporter.reflectFormValue(form, body)
            supporter.findInput(body, "account").alwaysPresent(input -> {
                input.setValue(form.account);
            });
        }
        supporter.findInput(body, "rememberMe").alwaysPresent(input -> {
            input.setChecked(form.rememberMe ? "on" : null); // #pending to easy setting
        });
    }
}
