package org.hyperledger.fabric.samples.assettransfer;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/**
 * @author zhouwenhua02
 * @date 2022/2/23
 */
@DataType
public class Order {

    @Property()
    private final String orderId;

    @Property()
    private final String purchaser;

    @Property()
    private final String business;

    @Property()
    private final String assetId;

    @Property()
    private final boolean confirm;

    public Order(@JsonProperty("orderId") final String orderId,
                 @JsonProperty("purchaser") final String purchaser,
                 @JsonProperty("business") final String business,
                 @JsonProperty("assetId") final String assetId,
                 @JsonProperty("confirm") final boolean confirm) {
        this.orderId = orderId;
        this.purchaser = purchaser;
        this.business = business;
        this.assetId = assetId;
        this.confirm = confirm;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPurchaser() {
        return purchaser;
    }

    public String getBusiness() {
        return business;
    }

    public boolean isConfirm() {
        return confirm;
    }

    public String getAssetId() {
        return assetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (confirm != order.confirm) return false;
        if (orderId != null ? !orderId.equals(order.orderId) : order.orderId != null) return false;
        if (purchaser != null ? !purchaser.equals(order.purchaser) : order.purchaser != null) return false;
        if (business != null ? !business.equals(order.business) : order.business != null) return false;
        return assetId != null ? assetId.equals(order.assetId) : order.assetId == null;
    }

    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (purchaser != null ? purchaser.hashCode() : 0);
        result = 31 * result + (business != null ? business.hashCode() : 0);
        result = 31 * result + (assetId != null ? assetId.hashCode() : 0);
        result = 31 * result + (confirm ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", purchaser='" + purchaser + '\'' +
                ", business='" + business + '\'' +
                ", assetId='" + assetId + '\'' +
                ", confirm=" + confirm +
                '}';
    }
}
