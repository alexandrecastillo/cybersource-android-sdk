package com.visa.inappsdk.datamodel;

import com.visa.inappsdk.datamodel.transaction.SDKTransactionObject;
import com.visa.inappsdk.datamodel.transaction.callbacks.SDKApiConnectionCallback;

/**
 * Abstract Gateway API
 * 
 * @author fzubair
 */
public abstract class SDKGateway {

	private static SDKGateway gateway;

	/**
	 *
	 * @return Instance of the Gateway
	 */
	public static SDKGateway getGateway() {
/*		if (gateway == null) {
			gateway = new InAppGateway();
		}*/
		return gateway;
	}

    /**
     * Sets the new gateway to be used for all server calls.
     *
     * @param gatewayInstance a gateway instance to be set
     */
	public static void setGateway(SDKGateway gatewayInstance) {
		gateway = gatewayInstance;
	}


	/**
	 * Performs payment Authorization for the transaction object
	 *
	 * during operation
	 * @return true if a transaction is already in progress
	 */
	public abstract boolean performEncryption(SDKTransactionObject transactionObject,
                                                 SDKApiConnectionCallback applicationConnectionCallback);

	/**
	 * Cleans the gateway instance.
	 */
	protected static void dispose() {
		gateway = null;
	};

}