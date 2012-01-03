var jrEngage;
function testPlugin()
{
    //navigator.notification.alert("testPlugin() function");

    jrEngage.print
    (
        ["HelloxWorld"],

        function(result)
        {
            alert("Success : \r\n"+result);
        },

        function(error)
        {
            alert("Error : \r\n"+error);
        }
    );
}

function onBodyLoad()
{
    document.addEventListener("deviceready", onDeviceReady, false);
}

/* When this function is called, Phonegap has been initialized and is ready to roll */
/* If you are supporting your own protocol, the var invokeString will contain any arguments to the app
launch.
see http://iphonedevelopertips.com/cocoa/launching-your-own-application-via-a-custom-url-scheme.html
for more details -jm */
function onDeviceReady()
{
    // do your thing!
    //navigator.notification.alert("Phonegap is working");

    jrEngage = window.plugins.jrEngagePlugin;

    jrEngage.initialize(
        "appcfamhnpkagijaeinl",
        "http://jrauthenticate.appspot.com/login",

        function(result)
        {
//            alert("Success: \n"+result);
        },

        function(error)
        {
//            alert("Error: \n"+error);
        }
    );
}

function showAuthenticationDialog()
{
    jrEngage.showAuthentication(
        function(result)
        {
            alert("Success: \n"+result);
        },

        function(error)
        {
            alert("Error: \n"+error);
        }
    );
}