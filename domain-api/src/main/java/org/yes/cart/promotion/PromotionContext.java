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

package org.yes.cart.promotion;

import org.yes.cart.domain.entity.Customer;
import org.yes.cart.shoppingcart.MutableShoppingCart;
import org.yes.cart.shoppingcart.Total;

import java.io.Serializable;
import java.time.Instant;

/**
 * Context object that holds runtime information necessary for the
 * promotion service to determine promotion eligibility.
 *
 * User: denispavlov
 * Date: 13-10-11
 * Time: 12:03 PM
 */
public interface PromotionContext extends Serializable {

    /**
     * @return shop code
     */
    String getShopCode();

    /**
     * @return timestamp for creation of this context
     */
    Instant getTimestamp();

    /**
     * Apply item level promotions on cart.
     *
     * @param customer customer
     * @param cart cart
     */
    void applyItemPromo(Customer customer, MutableShoppingCart cart);

    /**
     * Apply order level promotions on cart.
     *
     * @param customer customer
     * @param cart cart
     * @param itemTotal current total after item promotions
     *
     * @return  order total (does not include shipping promotions)
     */
    Total applyOrderPromo(Customer customer, MutableShoppingCart cart, Total itemTotal);

    /**
     * Apply shipping promotions on cart.
     *
     * @param customer customer
     * @param cart cart
     * @param orderTotal current total after order promotions
     */
    void applyShippingPromo(Customer customer, MutableShoppingCart cart, Total orderTotal);

    /**
     * Apply promotion on customer. This is not strictly speaking promotion
     * but rather customer segmentation.
     *
     * @param customer customer object to evaluate
     * @param cart (optional) current shopping cart. This is an extension mechanism
     *             if anyone would want to use this on storefront for some custom
     *             events
     */
    void applyCustomerPromo(Customer customer, MutableShoppingCart cart);
}
