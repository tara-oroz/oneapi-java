package oneapi.client.impl;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import oneapi.client.HLRClient;
import oneapi.client.impl.OneAPIBaseClientImpl;
import oneapi.config.Configuration;
import oneapi.exception.RequestException;
import oneapi.listener.HLRNotificationsListener;
import oneapi.model.RoamingNotification;
import oneapi.model.SubscribeToHLRDeliveryNotificationsRequest;
import oneapi.model.common.DeliveryReceiptSubscription;
import oneapi.model.common.DeliveryReportSubscription;
import oneapi.model.common.Roaming;
import oneapi.pushserver.PushServerSimulator;


public class HLRClientImpl extends OneAPIBaseClientImpl implements HLRClient {
	private static final String DATA_CONNECTION_PROFILE_URL_BASE = "/terminalstatus/queries";
	private static final String DATA_CONNECTION_PROFILE_SUBSCRIPTION_URL_BASE = "/smsmessaging/hlr/subscriptions";

	private volatile List<HLRNotificationsListener> hlrPushListenerList = null;
	private PushServerSimulator hlrPushServerSimulator;

	public HLRClientImpl(Configuration configuration) {
		super(configuration);
	}

	/**
	 * Query the customer’s roaming status for a single network-connected mobile device and get HLR to the specified notify url
	 * @param address (mandatory) mobile device number being queried
	 * @param notifyURL (mandatory) URL to receive the roaming status asynchronously
	 * @param clientCorrelator (optional) Active only if notifyURL is specified, otherwise ignored. Uniquely identifies this request. If there is a communication failure during the request, using the same clientCorrelator when retrying the request helps the operator to avoid call the same request twice.
	 * @param callbackData (optional) Active only if notifyURL is specified, otherwise ignored. This is custom data to pass back in notification to notifyURL, so you can use it to identify the request or any other useful data, such as a function name.
	 */
	@Override 
	public void queryHLR(String address, String notifyURL, String clientCorrelator, String callbackData) {	
		if (notifyURL == null || notifyURL.isEmpty()) {
			throw new RequestException("'notifiyURL' parmeter is mandatory using asynchronous method.");
		}

		StringBuilder urlBuilder = new StringBuilder(DATA_CONNECTION_PROFILE_URL_BASE);	
		urlBuilder.append("/roamingStatus?address=");	
		urlBuilder.append(encodeURLParam(address));
		urlBuilder.append("&includeExtendedData=true");	
		urlBuilder.append("&notifyURL=");
		urlBuilder.append(encodeURLParam(notifyURL));

		if (clientCorrelator != null && clientCorrelator.length() > 0)
		{
			urlBuilder.append("&clientCorrelator=");
			urlBuilder.append(encodeURLParam(clientCorrelator));
		}

		if (callbackData != null && callbackData.length() > 0)
		{
			urlBuilder.append("&callbackData=");
			urlBuilder.append(encodeURLParam(callbackData));
		}

		HttpURLConnection connection = executeGet(appendMessagingBaseUrl(urlBuilder.toString()));
		validateResponse(connection, getResponseCode(connection), RESPONSE_CODE_200_OK);	
	}

	/**
	 * Query the customer’s roaming status for a single network-connected mobile device and get HLR to the specified notify url
	 * @param address (mandatory) mobile device number being queried
	 * @param notifyURL (mandatory) URL to receive the roaming status asynchronously
	 */
	@Override 
	public void queryHLR(String address, String notifyURL) {	
		queryHLR(address, notifyURL, null, null);
	}

	/**
	 * Query the customer’s roaming status for a single network-connected mobile device and get HLR as the response
	 * @param address (mandatory) mobile device number being queried
	 * @return Roaming
	 */
	@Override
	public Roaming queryHLR(String address) {		
		StringBuilder urlBuilder = new StringBuilder(DATA_CONNECTION_PROFILE_URL_BASE);	
		urlBuilder.append("/roamingStatus?address=");	
		urlBuilder.append(encodeURLParam(address));
		urlBuilder.append("&includeExtendedData=true");	

		HttpURLConnection connection = executeGet(appendMessagingBaseUrl(urlBuilder.toString()));
		return deserialize(connection, Roaming.class, RESPONSE_CODE_200_OK, "roaming");	
	}

	/**
	 * Convert JSON to HLR Notification </summary>
	 * @param json
	 * @return RoamingNotification
	 */
	public RoamingNotification convertJsonToHLRNotificationExample(String json)
	{    
		return convertJSONToObject(json.getBytes(), RoamingNotification.class, "terminalRoamingStatusList");
	}

	/**
	 * Start subscribing to HLR delivery notifications over OneAPI         
	 * @param subscribeToHLRDeliveryNotificationsRequest
	 * @return String subscriptionId
	 */
	@Override
	public String subscribeToHLRDeliveryNotifications(SubscribeToHLRDeliveryNotificationsRequest subscribeToHLRDeliveryNotificationsRequest) {
		HttpURLConnection connection = executePost(appendMessagingBaseUrl(DATA_CONNECTION_PROFILE_SUBSCRIPTION_URL_BASE), subscribeToHLRDeliveryNotificationsRequest);
		DeliveryReceiptSubscription deliveryReceiptSubscription = deserialize(connection, DeliveryReceiptSubscription.class, RESPONSE_CODE_201_CREATED, "deliveryReceiptSubscription");
		return GetIdFromResourceUrl(deliveryReceiptSubscription.getResourceURL()); 
	}

	/**
	 * Get HLR delivery notifications subscriptions by subscription id
	 * @param subscriptionId
	 * @return DeliveryReportSubscription[]
	 */
	@Override
	public DeliveryReportSubscription[] getHLRDeliveryNotificationsSubscriptionsById(String subscriptionId) {
		StringBuilder urlBuilder = new StringBuilder(DATA_CONNECTION_PROFILE_SUBSCRIPTION_URL_BASE).append("/");
		urlBuilder.append(encodeURLParam(subscriptionId));

		HttpURLConnection connection = executeGet(appendMessagingBaseUrl(urlBuilder.toString()));
		return deserialize(connection, DeliveryReportSubscription[].class, RESPONSE_CODE_200_OK, "deliveryReceiptSubscriptions");
	}


	/**
	 * Stop subscribing to HLR delivery notifications over OneAPI 
	 * @param subscriptionId (mandatory) contains the subscriptionId of a previously created HLR delivery receipt subscription
	 */
	@Override
	public void removeHLRDeliveryNotificationsSubscription(String subscriptionId) {
		StringBuilder urlBuilder = new StringBuilder(DATA_CONNECTION_PROFILE_SUBSCRIPTION_URL_BASE).append("/");
		urlBuilder.append(encodeURLParam(subscriptionId));

		HttpURLConnection connection = executeDelete(appendMessagingBaseUrl(urlBuilder.toString()));
		validateResponse(connection, getResponseCode(connection), RESPONSE_CODE_204_NO_CONTENT);
	}

	/**
	 * Add OneAPI PUSH 'HLR' Notifications listener and start push server simulator
	 * @param listener
	 */
	public void addPushHLRNotificationsListener(HLRNotificationsListener listener)
	{
		if (listener == null)
		{
			return;
		}

		if (hlrPushListenerList == null)
		{
			hlrPushListenerList = new ArrayList<HLRNotificationsListener>();
		}

		hlrPushListenerList.add(listener);

		startHLRPushServerSimulator();

		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("Listener is successfully added, push server is started and is waiting for HLR Notifications");
		}
	}

	/**
	 * Returns HLR Notifications PUSH Listeners list
	 * @return List<HLRNotificationsListener>
	 */
	public List<HLRNotificationsListener> getHLRPushNotificationListeners()
	{
		return hlrPushListenerList;    
	}

	/**
	 *  Remove PUSH HLR listeners and stop server
	 */
	public void removePushHLRNotificationsListeners()
	{
		stopHLRPushServerSimulator();
		hlrPushListenerList = null;

		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("HLR Listeners are successfully removed.");
		}
	}

	private void startHLRPushServerSimulator()
	{
		if (hlrPushServerSimulator == null)
		{
			hlrPushServerSimulator = new PushServerSimulator(this, getConfiguration().getHlrPushServerSimulatorPort());
			hlrPushServerSimulator.start();
		} 
	}

	private void stopHLRPushServerSimulator()
	{
		if (hlrPushServerSimulator != null)
		{
			hlrPushServerSimulator.stop();   
		}
	}
}