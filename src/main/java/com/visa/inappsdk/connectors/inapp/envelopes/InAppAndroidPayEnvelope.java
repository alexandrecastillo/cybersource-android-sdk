package com.visa.inappsdk.connectors.inapp.envelopes;

import com.visa.inappsdk.common.error.SDKError;
import com.visa.inappsdk.common.utils.SDKUtils;
import com.visa.inappsdk.connectors.inapp.datamodel.InAppBillTo;
import com.visa.inappsdk.connectors.inapp.datamodel.InAppEncryptedPayment;
import com.visa.inappsdk.connectors.inapp.datamodel.InAppItem;
import com.visa.inappsdk.connectors.inapp.datamodel.InAppPurchaseTotals;
import com.visa.inappsdk.connectors.inapp.responses.InAppResponseObject;
import com.visa.inappsdk.connectors.inapp.services.InAppAuthService;
import com.visa.inappsdk.connectors.inapp.transaction.InAppEnvelopeAndroidPayTransactionObject;
import com.visa.inappsdk.connectors.inapp.transaction.InAppEnvelopeEncryptionTransactionObject;
import com.visa.inappsdk.connectors.inapp.transaction.client.InAppTransaction;
import com.visa.inappsdk.datamodel.response.SDKGatewayResponseType;
import com.visa.inappsdk.datamodel.transaction.fields.SDKBillTo;
import com.visa.inappsdk.datamodel.transaction.fields.SDKLineItem;
import com.visa.inappsdk.datamodel.transaction.fields.SDKPurchaseOrder;
import com.visa.inappsdk.soap.model.SDKXMLParentNode;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fzubair on 11/18/2015.
 */
public class InAppAndroidPayEnvelope extends InAppBaseEnvelope{

    InAppAndroidPayEnvelope() {
    }

    public InAppAndroidPayEnvelope(InAppTransaction transactionObject, String merchantId, String messageSignature) {
        createEnvelopeHeader(merchantId, messageSignature);
        InAppEnvelopeAndroidPayTransactionObject androidPayTransactionObject = convertTransactionObject(transactionObject, merchantId);
        createEnvelopeBody(androidPayTransactionObject);
    }

    private void createEnvelopeBody(InAppEnvelopeAndroidPayTransactionObject paymentObject) {
        SDKXMLParentNode request = this.createRequestMessage();
        paymentObject.updateEnvelope(request);
    }

    private InAppEnvelopeAndroidPayTransactionObject convertTransactionObject(InAppTransaction transactionObject,
                                                                              String merchantId) {
        String merchantReferenceCode = transactionObject.getMerchantReferenceCode();

        SDKBillTo billTo = transactionObject.getBillTo();
        InAppBillTo bill = null;
        if (billTo != null) {
            bill = new InAppBillTo(billTo.getFirstName(), billTo.getLastName(),
                    billTo.getEmail(), billTo.getPostalCode(), billTo.getStreet1(),
                    billTo.getStreet2(), billTo.getCity(), billTo.getState(),
                    billTo.getCountry());
        }

        InAppAuthService inAppAuthService  = new InAppAuthService(true, false);

        SDKPurchaseOrder purchaseOrder = transactionObject.getPurchaseOrder();
        List<InAppItem> items = null;
        InAppPurchaseTotals purchaseTotals = null;
        if(purchaseOrder != null) {
            purchaseTotals = new InAppPurchaseTotals(
                    purchaseOrder.getCurrency().name(),
                    SDKUtils.getGatewayAmountStringFromBigDecimal
                            (purchaseOrder.getGrandTotalAmount())
            );
            List<SDKLineItem> lineItems = purchaseOrder.getLineItems();
            if (lineItems != null) {
                items = new ArrayList<>();
                int id = 0;
                for (SDKLineItem item : lineItems) {
                    String itemId = String.valueOf(id);

                    InAppItem newItem = new InAppItem(itemId, String.valueOf(item.getUnitPrice()),
                            String.valueOf(item.getQuantity()), null, item.getProductName(), null,
                            String.valueOf(item.getTaxAmount()));
                    items.add(newItem);
                    id++;
                }
            }
        }

        String encryptedPaymentData = transactionObject.getEncryptedPaymentData();
        // TODO: CREATE SEC BLOB OUT OF THIS ANDROID PAY BLOB
        String secBlobEncryptedPayment = createSecServiceJson(encryptedPaymentData);
        secBlobEncryptedPayment = SDKUtils.getBase64Blob(secBlobEncryptedPayment);

        InAppEncryptedPayment inAppEncryptedPayment = new InAppEncryptedPayment
                (DESCRIPTOR_FID, secBlobEncryptedPayment);

        InAppEnvelopeAndroidPayTransactionObject inAppEnvelopeAndroidPayTransactionObject =
                new InAppEnvelopeAndroidPayTransactionObject(merchantId, merchantReferenceCode,
                        bill, items, purchaseTotals, inAppAuthService, PAYMENT_SOLUTION, CLIENT_LIBRARY,
                        inAppEncryptedPayment);
        return inAppEnvelopeAndroidPayTransactionObject;
    }

    private String createSecServiceJson(String androidPayBlob){
        String secBlob = "{\"publicKeyHash\": \"" + SDKUtils.getPublicKeyHash() + "\"," +
                "\"version\": \"1.0\"," +
                "\"data\":" + "\"" + androidPayBlob + "\"}";
        return secBlob;
    }


    @Override
    protected void createEnvelopeHeader(String merchantID, String messageSignature) {
        super.createEnvelopeHeader(merchantID, messageSignature);
    }

    @Override
    public SDKError parseGatewayError(InputStream inputStream) {
        return super.parseGatewayError(inputStream);
    }

    @Override
    public InAppResponseObject parseResponse(InputStream inputStream) {
        return InAppResponseObject.createAndroidPayAuthResponse(inputStream, getResponseType());
    }

    @Override
    public SDKGatewayResponseType getResponseType() {
        return SDKGatewayResponseType.SDK_ANDROID_PAY;
    }
}