/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.math.BigDecimal;
import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Asset {

    @Property()
    private final String assetID;

    @Property()
    private final String type;

    @Property()
    private final BigDecimal price;

    @Property()
    private final String owner;

    public String getAssetID() {
        return assetID;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getOwner() {
        return owner;
    }

    public Asset(@JsonProperty("assetID") final String assetID, @JsonProperty("type") final String type,
            @JsonProperty("price") final BigDecimal price, @JsonProperty("owner") final String owner) {
        this.assetID = assetID;
        this.type = type;
        this.price = price;
        this.owner = owner;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Asset other = (Asset) obj;

        return Objects.deepEquals(
                new String[] {getAssetID(), getType(), getOwner()},
                new String[] {other.getAssetID(), other.getType(), other.getOwner()})
                &&
                Objects.deepEquals(
                        new BigDecimal[] {getPrice()},
                        new BigDecimal[] {other.getPrice()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAssetID(), getType(), getPrice(), getOwner());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [assetID=" + assetID + ", type="
                + type + ", price=" + price + ", owner=" + owner + "]";
    }
}
