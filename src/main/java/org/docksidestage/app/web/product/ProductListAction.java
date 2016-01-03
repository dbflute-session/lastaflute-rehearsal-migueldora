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
package org.docksidestage.app.web.product;

import javax.annotation.Resource;

import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.optional.OptionalThing;
import org.docksidestage.app.web.base.MigueldoraBaseAction;
import org.docksidestage.dbflute.exbhv.ProductBhv;
import org.docksidestage.dbflute.exentity.Product;
import org.lastaflute.web.Execute;
import org.lastaflute.web.login.AllowAnyoneAccess;
import org.lastaflute.web.response.HtmlResponse;

/**
 * @author jflute
 */
@AllowAnyoneAccess
public class ProductListAction extends MigueldoraBaseAction {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private ProductBhv productBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute
    public HtmlResponse index(OptionalThing<Integer> pageNumber, ProductSearchForm form) {
        validate(form, messages -> {} , () -> {
            return asHtml(path_Product_ProductListHtml);
        });
        PagingResultBean<Product> page = selectProductPage(pageNumber.orElse(1), form);
        return asHtml(path_Product_ProductListHtml).withView(new ProductListView(form, page));
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    private PagingResultBean<Product> selectProductPage(int pageNumber, ProductSearchForm form) {
        verifyParameterTrue("The pageNumber should be positive number: " + pageNumber, pageNumber > 0);
        return productBhv.selectPage(cb -> {
            cb.ignoreNullOrEmptyQuery();
            cb.setupSelect_ProductStatus();
            cb.setupSelect_ProductCategory();
            cb.specify().derivedPurchase().max(purchaseCB -> {
                purchaseCB.specify().columnPurchaseDatetime();
            } , Product.ALIAS_latestPurchaseDate);
            cb.query().setProductName_LikeSearch(form.productName, op -> op.likeContain());
            final String purchaseMemberName = form.purchaseMemberName;
            if (isNotEmpty(purchaseMemberName)) {
                cb.query().existsPurchase(purchaseCB -> {
                    purchaseCB.query().queryMember().setMemberName_LikeSearch(purchaseMemberName, op -> op.likeContain());
                });
            }
            cb.query().setProductStatusCode_Equal_AsProductStatus(form.productStatus);
            cb.query().addOrderBy_ProductName_Asc();
            cb.query().addOrderBy_ProductId_Asc();
            cb.paging(getPagingPageSize(), pageNumber);
        });
    }
}
