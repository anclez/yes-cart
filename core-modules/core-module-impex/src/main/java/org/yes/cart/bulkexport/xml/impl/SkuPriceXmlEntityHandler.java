/*
 * Copyright 2009 Denys Pavlov, Igor Azarnyi
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yes.cart.bulkexport.xml.impl;

import org.yes.cart.bulkcommon.model.ImpExTuple;
import org.yes.cart.bulkcommon.xml.XmlValueAdapter;
import org.yes.cart.bulkexport.xml.XmlExportDescriptor;
import org.yes.cart.domain.entity.SkuPrice;
import org.yes.cart.service.async.JobStatusListener;

/**
 * User: denispavlov
 * Date: 04/11/2018
 * Time: 14:30
 */
public class SkuPriceXmlEntityHandler extends AbstractXmlEntityHandler<SkuPrice> {

    public SkuPriceXmlEntityHandler() {
        super("price-list");
    }

    @Override
    public String handle(final JobStatusListener statusListener,
                         final XmlExportDescriptor xmlExportDescriptor,
                         final ImpExTuple<String, SkuPrice> tuple,
                         final XmlValueAdapter xmlValueAdapter,
                         final String fileToExport) {

        return tagPrice(null, tuple.getData()).toXml();

    }

    Tag tagPrice(final Tag parent, final SkuPrice price) {

        return tag(parent, "price")
                .attr("id", price.getSkuPriceId())
                .attr("guid", price.getGuid())
                .attr("sku", price.getSkuCode())
                .attr("shop", price.getShop().getCode())
                .attr("currency", price.getCurrency())
                .attr("quantity", price.getQuantity())
                .attr("offer", price.isPriceOnOffer())
                .attr("request", price.isPriceUponRequest())
                .attr("generated", price.isAutoGenerated())
                .tag("pricing-policy")
                    .attr("policy", price.getPricingPolicy())
                    .attr("ref", price.getPricingPolicy())
                .end()
                .tagList("tags", "tag", price.getTag(), ' ')
                .tagNum("list-price", price.getRegularPrice())
                .tagNum("sale-price", price.getSalePrice())
                .tagNum("minimal-price", price.getMinimalPrice())
                .tag("availability")
                    .tagTime("available-from", price.getSalefrom())
                    .tagTime("available-to", price.getSaleto())
                .end()
                .tagTime(price)
                .end();

    }

}
