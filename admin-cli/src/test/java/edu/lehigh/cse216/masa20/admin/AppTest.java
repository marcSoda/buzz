package edu.lehigh.cse216.masa20.admin;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    /**
     * Unit test done by Jake F to test RowData constructor
     */
    public void testRowDataConstructor()
    {
        Database.RowData test = new Database.RowData(1, "subject", "this is a message", "20:30", 6, 2);
        assertTrue(test.mId == 1);
        assertTrue(test.mSubject.equals("subject"));
        assertTrue(test.mMessage.equals("this is a message"));
        assertTrue(test.mTimestamp.equals("20:30"));
        assertTrue(test.mUpvotes == 6);
        assertTrue(test.mDownvotes == 2);
    }

}
