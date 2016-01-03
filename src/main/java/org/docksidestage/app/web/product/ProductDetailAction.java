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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.docksidestage.app.web.base.MigueldoraBaseAction;
import org.docksidestage.dbflute.allcommon.CDef.ProductStatus;
import org.docksidestage.dbflute.exbhv.ProductBhv;
import org.docksidestage.dbflute.exentity.Product;
import org.lastaflute.web.Execute;
import org.lastaflute.web.login.AllowAnyoneAccess;
import org.lastaflute.web.response.HtmlResponse;

/**
 * @author jflute
 */
@AllowAnyoneAccess
public class ProductDetailAction extends MigueldoraBaseAction {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          DI Component
    //                                          ------------
    @Resource
    private ProductBhv productBhv;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute
    public HtmlResponse index(Integer productId) {
        validate(productId, messages -> {} , () -> {
            return asHtml(path_Product_ProductListHtml);
        });
        Product product = selectProduct(productId);
        List<ProductDetailBean> productList =
                searchRecommendProducdtList(productId).stream().map(this::mappingToBean).collect(Collectors.toList());
        return asHtml(path_Product_ProductDetailHtml).renderWith(data -> {
            data.register("product", mappingToBean(product));
            data.register("reccomendProductList", productList);
        });
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    private Product selectProduct(int productId) {
        return productBhv.selectEntity(cb -> {
            cb.setupSelect_ProductCategory();
            cb.query().setProductId_Equal(productId);
        }).orElseThrow(() -> {
            return of404("Not found the product: " + productId); // mistake or user joke
        });
    }

    private List<Product> searchRecommendProducdtList(Integer productId) {
        return productBhv.selectList(cb -> {
            cb.setupSelect_ProductCategory();
            cb.query().queryProductCategory().existsProduct(prdCb -> {
                prdCb.query().setProductId_Equal(productId);
            });
            cb.query().setProductStatusCode_InScope_AsProductStatus(Arrays.asList(ProductStatus.OnSaleProduction));
            cb.specify().derivedPurchase().sum(purchseCB -> {
                purchseCB.specify().columnPurchaseCount();
            } , Product.ALIAS_purchaseCount);
            cb.query().addSpecifiedDerivedOrderBy_Desc(Product.ALIAS_purchaseCount);
            cb.fetchFirst(5);
        });
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private ProductDetailBean mappingToBean(Product product) {
        ProductDetailBean bean = new ProductDetailBean();
        bean.productId = product.getProductId();
        bean.productName = product.getProductName();
        bean.regularPrice = product.getRegularPrice();
        bean.productHandleCode = product.getProductHandleCode();
        product.getProductCategory().alwaysPresent(category -> {
            bean.categoryName = category.getProductCategoryName();
        });
        return bean;
    }

}
