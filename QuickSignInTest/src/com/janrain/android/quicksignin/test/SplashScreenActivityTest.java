/**
 * 
 */
package com.janrain.android.quicksignin.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import com.janrain.android.quicksignin.SplashScreen;

/**
 * @author mitch
 *
 */
public class SplashScreenActivityTest extends
		ActivityInstrumentationTestCase2<SplashScreen> {

	private SplashScreen mActivity;
	private Button mBtnViewProfiles;

	public SplashScreenActivityTest() {
		super(SplashScreen.class);
	}

	/**
	 * @throws java.lang.Exception
	 */
	protected static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	protected static void tearDownAfterClass() throws Exception {
	}

	/* (non-Javadoc)
	 * @see android.test.ActivityInstrumentationTestCase2#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

	    mActivity = getActivity();

	    mBtnViewProfiles =
	      (Button) mActivity.findViewById(
	    		  com.janrain.android.quicksignin.R.id.btn_view_profiles
	      );
	}

	public void testPreConditions() {
		assertTrue(mBtnViewProfiles != null);
	}

	/* (non-Javadoc)
	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link com.janrain.android.quicksignin.SplashScreen#onCreate(android.os.Bundle)}.
	 */
	public final void testOnCreateBundle() {
		// fail("Not yet implemented"); // TODO
	}

}
