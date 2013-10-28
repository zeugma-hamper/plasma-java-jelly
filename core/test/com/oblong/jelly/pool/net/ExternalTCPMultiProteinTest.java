package com.oblong.jelly.pool.net;

import com.oblong.jelly.*;
import com.oblong.jelly.communication.ObPoolCommunicationEventHandler;
import com.oblong.jelly.communication.ObPoolConnector;
import com.oblong.jelly.util.ExceptionHandler;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: valeria
 * Date: 10/1/13
 * Time: 1:37 PM
 *
 * This class allows to test TCP Hoses waitNext (timeout, unit)
 *
 * using url localhost
 *
 * the pool name is provided bt you can modify, it will be created
 *
 * Exceptions are logged and printed or not (setup the flag)
 *
 * set up NUMBER_OF_DEPOSITED_PROTEINS_IN_BATCH to handle more or less proteins
 */
public class ExternalTCPMultiProteinTest {

	private static final Logger logger = Logger.getLogger(ExternalTCPMultiProteinTest.class);

	/***if set to false exceptions/logs will no be logged ***/
	private static final List<Protein> toSendProteinQueue = Collections
			.synchronizedList(new LinkedList<Protein>());

	/**
	 * Property "test.server" for compatibility with what is already in Ant/JUnit
	 * To set it: e.g. -Dtest.server=tcp://10.3.10.111
	 */
	public static final String TEST_SERVER_PROPERTY = "test.server";


	private static ExternalHoseTests tests;
	private static JellyTestPoolSender senderThread;
	private static ObHandler listener =  new ObHandler();
	private static final String TAG = "ExternalTCPMultiProteinTest";

	public static final String POOL_NAME = "external-tests-pool";

	/***
	 * used for the endless test
	 */
	private static boolean stopTest = false;

	@BeforeClass
	public static void setUp()  {
		setUpBeforeTest();
	}

	private static void setUpBeforeTest() {
		if (ExternalTCPMultiProteinTestConfig.SHOW_LOGS) {
			ExceptionHandler.setExceptionHandler(new TestExceptionHandler());
		}

		try {
			final PoolServerAddress poolServerAddress = getPoolServerAddress();
			logger.info("Will test with pool server address "+poolServerAddress.toString());
			final PoolAddress poolAddress = new PoolAddress(poolServerAddress, POOL_NAME);
			//create pool otherwise test will fail
			createPoolIfNonExisting(poolAddress);

			senderThread = new JellyTestPoolSender(poolServerAddress,
					POOL_NAME,
					listener,
					toSendProteinQueue,
					null);

			tests = new ExternalHoseTests(poolServerAddress,
					ExternalTCPMultiProteinTestConfig.getTotalNumberOfProteins(), POOL_NAME);
		} catch (BadAddressException e){
			//something wrong with server?
			handleAndFail(e);
		} catch (PoolException e){
			//something wrong with server?
			handleAndFail(e);
		}
	}


	private static void handleAndFail(Exception e) {
		ExceptionHandler.handleException(e);
		fail("Unable to connect to pool server, you need a running pool server and g-speak installed");
	}

	static void createPoolIfNonExisting(PoolAddress poolAddress) throws PoolException {
		if(!Pool.exists(poolAddress)){
			Pool.create(poolAddress, ObPoolConnector.DEFAULT_POOL_OPTIONS);
		}
	}

	static PoolServerAddress getPoolServerAddress() throws BadAddressException {
		String uri = System.getProperty("com.oblong.jelly.externalServer", ExternalTCPMultiProteinTestConfig.getDefaultUri());
		return PoolServerAddress.fromURI(uri);
	}

	/***
	 * Tests receiving multiple proteins
	 */
	@Test
	public void awaitNext()  {
		testAwaitNext();
	}

	private static void testAwaitNext() {
		try {
			if(tests!=null){
				startAwaitNextThenSender();
			} else {
				// FIXME shouldn't this be fatal?
				ExceptionHandler.handleException("Tests not initiated");
			}
		} catch (PoolException e) {
			// FIXME shouldn't this be fatal?
			ExceptionHandler.handleException(e, " tests.awaitNext() failed on round +" +tests.getLastExecutedRound());
		}
	}

	private static void startAwaitNextThenSender() throws PoolException {
		tests.awaitNext();
		senderThread.start();
	}

//	static void logErrorMessage(String errorMessage) {
//		if(ExternalTCPMultiProteinTestConfig.SHOW_LOGS){
//			System.err.println(errorMessage);
//		}
//	}

//	static void logMessage(String message) {
//		if(ExternalTCPMultiProteinTestConfig.SHOW_LOGS){
//			System.out.println(message);
//		}
//	}

	@AfterClass
	public static void afterTesting(){
		cleanUpAfterTest();
	}

	private static void cleanUpAfterTest() {
		try {

			logger.info(" tests.awaitNext() finished on round " + tests.getLastExecutedRound());
//			logMessage(" last received protein " + tests.getLastObtainedProtein());
			senderThread.halt();
			tests.cleanUp();
		} catch (Exception e){
			ExceptionHandler.handleException(e);
		}
	}

	public static class ObHandler implements ObPoolCommunicationEventHandler {

		@Override
		public void onProteinReceived(Protein p) {
			//ignored here
		}

		@Override
		public void onConnected() {
			logger.info(TAG + " Hose connected correctly");

		}

		@Override
		public void onErrorConnecting() {
			logger.error(TAG + " Error connecting sending hose ");
		}

		@Override
		public void onConnectionLost(String reason) {
			logger.warn(TAG + " Connection lost to sending hose ");
		}

	}

	public static class TestExceptionHandler extends ExceptionHandler {

		public TestExceptionHandler() {	}

		@Override
		public void handleExceptionImpl(Throwable e, String syntheticMsg)  {
			System.err.println("====== " + syntheticMsg);
			e.printStackTrace();
		}

	}

	/****Main Method to run class outside Junit framework***/
	public static void main(String[] args) {
		DOMConfigurator.configure("tests_log4j_conf.xml");

		logger.info("Starting test via main() method");

		String testServer = getProperty(TEST_SERVER_PROPERTY);

		ExternalTCPMultiProteinTestConfig.settingsForMultiProteinTest.setUriForTest(testServer);

		//runs only one test no disconnect/reconnect
		//runOneEndlessTest();

		//with connect/disconnect
		stopTest = false;
		runConnectDisconnectEndlessTest();
	}

	private static String getProperty(String propertyName) {
		String propertyValue = System.getProperty(propertyName);
		logger.info("System property " + propertyName + ": " + propertyValue);
		return propertyValue;
	}

	private static void runConnectDisconnectEndlessTest() {
		try {
//			ExternalTCPMultiProteinTestConfig.settingsForMultiProteinTest.setUriForTest("tcp://10.3.10.111");
			ExternalTCPMultiProteinTestConfig.INFINITE_TEST = false;
			while(!stopTest){
				setUpBeforeTest();
				startAwaitNextThenSender();
				cleanUpAfterTest();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error("Thread.sleep Interrupted", e);
					stopTest = true;
					//ExceptionHandler.handleException(e);
				}
			}
			cleanUpAfterTest();
			logger.info("Test finished");
		} catch (Throwable e) {
			logger.error("Exception", e);
			stopTest = true;
			cleanUpAfterTest();
			throw new RuntimeException(e);
		}
	}

	private static void runOneEndlessTest() {
		ExternalTCPMultiProteinTestConfig.setDefaultTestSettingsForEndlessTest();

		setUpBeforeTest();
		testAwaitNext();
		cleanUpAfterTest();
	}

}
