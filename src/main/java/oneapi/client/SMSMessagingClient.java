package oneapi.client;

import java.util.List;

import oneapi.listener.DeliveryReportListener;
import oneapi.listener.DeliveryStatusNotificationsListener;
import oneapi.listener.InboundMessageListener;
import oneapi.listener.InboundMessageNotificationsListener;
import oneapi.model.*;
import oneapi.model.common.DeliveryInfoList;
import oneapi.model.common.DeliveryReport;
import oneapi.model.common.DeliveryReportSubscription;
import oneapi.model.common.InboundSMSMessageList;
import oneapi.model.common.MoSubscription;


public interface SMSMessagingClient {

	/**
	 * Send an SMS to one or more mobile terminals using the customized SMS object 
	 * @param sms - object containing data needed to be filled in order to send the SMS
	 * @return String Request Id
	 */
	String sendSMS(SMSRequest sms);

	/**
	 * Query the delivery status for an SMS sent to one or more mobile terminals                        
	 * @param senderAddress (mandatory) is the address from which SMS messages are being sent. Do not URL encode this value prior to passing to this function
	 * @param requestId (mandatory) contains the requestId returned from a previous call to the sendSMS function 
	 * @return DeliveryInfoList
	 */
	DeliveryInfoList queryDeliveryStatus(String senderAddress, String requestId);
	
	 /**
     * Convert JSON to Delivery Info Notification </summary>
     * @param json
     * @return DeliveryInfoNotification
     */
    DeliveryInfoNotification convertJsonToDeliveryInfoNotification(String json);

	/**
	 * Start subscribing to delivery status notifications over OneAPI for all your sent SMS  	                          
	 * @return String Subscription Id
	 */
	// TODO(TK) Return value neka bude DeliveryReportSubscription s ispunjenim subscriptionId-om
	String subscribeToDeliveryStatusNotifications(SubscribeToDeliveryNotificationsRequest subscribeToDeliveryNotificationsRequest);

	/**
	 * Retrieve delivery notifications subscriptions by sender address
	 * @param senderAddress
	 * @return DeliveryReportSubscription[]
	 */
	DeliveryReportSubscription[] getDeliveryNotificationsSubscriptionsBySender(String senderAddress);

	/**
	 * Retrieve delivery notifications subscriptions by subscription id
	 * @param subscriptionId
	 * @return DeliveryReportSubscription
	 */
	DeliveryReportSubscription getDeliveryNotificationsSubscriptionById(String subscriptionId);

	/**
	 * Retrieve delivery notifications subscriptions by for the current user
	 * @return DeliveryReportSubscription[]
	 */
	DeliveryReportSubscription[] getDeliveryNotificationsSubscriptions();

	/**
	 * Stop subscribing to delivery status notifications for all your sent SMS  
	 * @param subscriptionId (mandatory) contains the subscriptionId of a previously created SMS delivery receipt subscription
	 */
	void removeDeliveryNotificationsSubscription(String subscriptionId);

	/**
	 * Retrieve SMS messages sent to your Web application over OneAPI
	 * @return InboundSMSMessageList
	 */
	InboundSMSMessageList getInboundMessages();

	/**
	 * Retrieve SMS messages sent to your Web application 
	 * @param  maxBatchSize (mandatory) is the maximum number of messages to get in this request
	 * @return InboundSMSMessageList
	 * @throws InboundMessagesException 
	 */
	InboundSMSMessageList getInboundMessages(int maxBatchSize);
	
	/**
     * Convert JSON to Inbound SMS Message Notification
     * @param json
     * @return InboundSMSMessageList
     */
    InboundSMSMessageList convertJsonToInboundSMSMessageNotificationExample(String json);
	
	/**
	 * Start subscribing to notifications of SMS messages sent to your application over OneAPI                           
	 * @param subscribeToInboundMessagesRequest (mandatory) contains inbound messages subscription data
	 * @return string - Subscription Id 
	 */
	// TODO(TK) Istražiti odnos između clientCorrelator i subscriptionId
     String subscribeToInboundMessagesNotifications(SubscribeToInboundMessagesRequest subscribeToInboundMessagesRequest);

	 /**
     * Retrieve inbound messages notifications subscriptions for the current user
     * @return MoSubscription[]
     */
    MoSubscription[] getInboundMessagesSubscriptions(int page, int pageSize);
    
    
    /**
     * Retrieve inbound messages notifications subscriptions for the current user (Default values are used: page=1, pageSize=10)
     * @return MoSubscription[]
     */
    MoSubscription[] getInboundMessagesSubscriptions();
	
	/**
	 * Stop subscribing to message receipt notifications for all your received SMS                       
	 * @param subscriptionId (mandatory) contains the subscriptionId of a previously created SMS message receipt subscription
	 */
	void removeInboundMessagesSubscription(String subscriptionId);

	  /**
     * Retrieve delivery reports
     * @param limit
     * @return DeliveryReport[]
     */
    DeliveryReport[] getDeliveryReports(int limit);
	
	/**
	 * Retrieve delivery reports 
	 * @return DeliveryReport[]
	 */
	DeliveryReport[] getDeliveryReports();

	 /**
     * Retrieve delivery reports by Request Id
     * @param requestId
     * @param limit
     * @return DeliveryReport[]
     */
    public DeliveryReport[] getDeliveryReportsByRequestId(String requestId, int limit);
	
	/**
	 * Retrieve delivery reports by Request Id
	 * @param requestId
	 * @return DeliveryReport[]
	 */
    DeliveryReport[] getDeliveryReportsByRequestId(String requestId);

	/**
	 * Add 'INBOUND Messages' listener
	 * 
	 * @param listener - (new InboundMessageListener)
	 */
	void addPullInboundMessageListener(InboundMessageListener listener);

	/**
	 * Add 'Delivery Reports' listener.
	 * @param listener - (new DeliveryReportListener)
	 */
	void addPullDeliveryReportListener(DeliveryReportListener listener);

	/**
	 * Returns INBOUND Message Listeners list
	 */
	List<InboundMessageListener> getInboundMessagePullListeners();

	/**
	 * Returns Delivery Reports Listeners list
	 */
	List<DeliveryReportListener> getDeliveryReportPullListeners();

	/**
	 * Remove Delivery Reports listeners and stop retriever
	 */

	void removePullDeliveryReportListeners();
	
	/**
	 * Remove INBOUND Messages listeners and stop retriever
	 */
	void removePullInboundMessageListeners();
	
	 /**
     * Add OneAPI PUSH 'Delivery Status' Notifications listener  and start push server simulator
     */
    void addPushDeliveryStatusNotificationListener(DeliveryStatusNotificationsListener listener);
    
    /**
     * Add OneAPI PUSH 'INBOUND Messages' Notifications listener and start push server simulator
     * @param listener
     */
    void addPushInboundMessageListener(InboundMessageNotificationsListener listener);
    
    /**
     * Returns Delivery Status Notifications PUSH Listeners list 
     * @return List<DeliveryStatusNotificationsListener>
     */
    List<DeliveryStatusNotificationsListener> getDeliveryStatusNotificationPushListeners();
    
    /**
     * Returns INBOUND Message Notifications PUSH Listeners list
     * @return List<InboundMessageNotificationsListener>
     */
    List<InboundMessageNotificationsListener> getInboundMessagePushListeners();
    
    /**
     *  Remove PUSH Delivery Reports Notifications listeners and stop server
     */
    void removePushDeliveryStatusNotificationListeners();
    
    /**
     *  Remove PUSH Delivery Reports Notifications listeners and stop server
     */
    void removePushInboundMessageListeners();
}