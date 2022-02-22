/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
        name = "basic",
        info = @Info(
                title = "Asset Transfer",
                description = "The hyperlegendary asset transfer",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "a.transfer@example.com",
                        name = "Adrian Transfer",
                        url = "https://hyperledger.example.com")))
@Default
public final class AssetTransfer implements ContractInterface {

    private final Genson genson = new Genson();

    private enum AssetTransferErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS
    }

    /**
     * Creates some initial assets on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        CreateAsset(ctx, "usera@org3.example.com-wallet", "money", new BigDecimal("5000.0"), "usera@org3.example.com");
        CreateAsset(ctx, "userb@org3.example.com-wallet", "money", new BigDecimal("5000.0"), "userb@org3.example.com");
        CreateAsset(ctx, "asset1", "water", new BigDecimal("1000.0"), "usera@org3.example.com");
        CreateAsset(ctx, "asset3", "medicine", new BigDecimal("6000.0"), "usera@org3.example.com");
        CreateAsset(ctx, "asset2", "clothes", new BigDecimal("2000.0"), "userb@org3.example.com");
        CreateAsset(ctx, "asset5", "glass", new BigDecimal("4000.0"), "User1@org1.example.com");
        CreateAsset(ctx, "asset6", "desk", new BigDecimal("3000.0"), "User1@org2.example.com");

    }

    /**
     * Creates a new asset on the ledger.
     *
     * @param ctx the transaction context
     * @param assetID the ID of the new asset
     * @param type the type of the new asset
     * @param price the price for the new asset
     * @param owner the owner of the new asset
     * @return the created asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset CreateAsset(final Context ctx, final String assetID, final String type, final BigDecimal price, final String owner) {
        ChaincodeStub stub = ctx.getStub();

        if (AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s already exists", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Asset asset = new Asset(assetID, type, price, owner);
        //Use Genson to convert the Asset into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(asset);
        stub.putStringState(assetID, sortedJson);

        return asset;
    }

    /**
     * Retrieves an asset with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param assetID the ID of the asset
     * @return the asset found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset ReadAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset asset = genson.deserialize(assetJSON, Asset.class);

        // 鉴权
        currentClientIsOwner(ctx, asset.getOwner());

        return asset;
    }

    /**
     * Updates the properties of an asset on the ledger.
     *
     * @param ctx the transaction context
     * @param assetID the ID of the asset being updated
     * @param type the type of the asset being updated
     * @param price the price of the asset being updated
     * @param owner the owner of the asset being updated
     * @return the transferred asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset UpdateAsset(final Context ctx, final String assetID, final String type, final BigDecimal price, final String owner) {
        ChaincodeStub stub = ctx.getStub();

        ReadAsset(ctx, assetID);

        Asset newAsset = new Asset(assetID, type, price, owner);
        //Use Genson to convert the Asset into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(assetID, sortedJson);
        return newAsset;
    }

    /**
     * Deletes asset on the ledger.
     *
     * @param ctx the transaction context
     * @param assetID the ID of the asset being deleted
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        ReadAsset(ctx, assetID);
        stub.delState(assetID);
    }

    /**
     * Checks the existence of the asset on the ledger
     *
     * @param ctx the transaction context
     * @param assetID the ID of the asset
     * @return boolean indicating the existence of the asset
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * Changes the owner of a asset on the ledger.
     *
     * @param ctx the transaction context
     * @param assetID the ID of the asset being transferred
     * @param newOwner the new owner
     * @return the old owner
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String TransferAsset(final Context ctx, final String assetID, final String newOwner) {
        ChaincodeStub stub = ctx.getStub();
        Asset asset = ReadAsset(ctx, assetID);

        String aWalletAssertId = asset.getOwner() + "-wallet";
        String bWalletAssertId = newOwner + "-wallet";

        Asset aWalletAsset = ReadAsset(ctx, aWalletAssertId);
        Asset bWalletAsset = ReadAsset(ctx, bWalletAssertId);
        if (bWalletAsset.getPrice().compareTo(asset.getPrice()) < 0) {
            throw new ChaincodeException(newOwner + "'s capital account is insufficient, and the account balance is remaining: " + bWalletAsset.getPrice() + ", the account needs at least " + asset.getPrice());
        }

        Asset newAsset = new Asset(asset.getAssetID(), asset.getType(), asset.getPrice(), newOwner);
        //Use a Genson to conver the Asset into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(assetID, sortedJson);

        // 暂时忽略一致性
        // b账户减去金额
        Asset bWalletNewAsset = new Asset(bWalletAsset.getAssetID(), bWalletAsset.getType(), bWalletAsset.getPrice().subtract(asset.getPrice()), newOwner);
        String bWalletNewJSON = genson.serialize(bWalletNewAsset);
        stub.putStringState(bWalletAsset.getAssetID(), bWalletNewJSON);

        // a账户加上金额
        Asset aWalletNewAsset = new Asset(aWalletAsset.getAssetID(), aWalletAsset.getType(), aWalletAsset.getPrice().add(asset.getPrice()), aWalletAsset.getOwner());
        String aWalletNewJSON = genson.serialize(aWalletNewAsset);
        stub.putStringState(aWalletAsset.getAssetID(), aWalletNewJSON);

        System.out.println("goods: [assetId: " + asset.getAssetID() + ", type: " + asset.getType() + ", price: " + asset.getPrice() + "]");
        System.out.println("owner: " + asset.getOwner() + " --> " + newOwner);
        System.out.println(asset.getOwner() + " Account balance: " + aWalletAsset.getPrice());
        System.out.println(newOwner + " Account balance: " + bWalletAsset.getPrice());

        String returnMsg = "\ngoods: [assetId: " + asset.getAssetID() + ", type: " + asset.getType() + ", price: "
                            + asset.getPrice() + "]\n"
                            + "owner: " + asset.getOwner() + " --> " + newOwner + "\n"
                            + asset.getOwner() + " Account balance: " + aWalletAsset.getPrice() + "\n"
                            + newOwner + " Account balance: " + bWalletAsset.getPrice() + "\n";
        return returnMsg;
    }

    /**
     * Retrieves all assets from the ledger.
     *
     * @param ctx the transaction context
     * @return array of assets found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAssets(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Asset> queryResults = new ArrayList<Asset>();

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Asset asset = genson.deserialize(result.getStringValue(), Asset.class);
            System.out.println(asset);
            queryResults.add(asset);
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

    private static void currentClientIsOwner(final Context ctx, final String owner) {

        final String clientId = ctx.getClientIdentity().getId();
        if (owner == null || owner.isEmpty()) {
            throw new ChaincodeException("auth failed, owner is " + owner + ", but current client is " + clientId);
        }
        // e.g. usera@org3.example.com
        final String[] tmp = owner.split("@");

        if (tmp.length != 2) {
            throw new ChaincodeException("auth failed, owner is " + owner + ", but current client is " + clientId);
        }

        if (clientId.contains("::CN=" + tmp[0]) && clientId.contains("::CN=ca." + tmp[1])) {
            return;
        }

        throw new ChaincodeException("auth failed, owner is " + owner + ", but current client is " + clientId);
    }
}
