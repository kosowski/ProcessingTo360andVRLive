Example for viewing in VR a Processing sketch rendering in 360.

The Processing sketch, located in the folder "Spout360Export", renders a 360 camera into a texture and sends it in real time, via Spout, to a simple Project in Unity, located in the folder "Spout360Player", so it can be viewed with a VR HMD.

The Processing sketch is taken from the "mono_360" folder from [this repo](https://github.com/tracerstar/processing-360-video) by Benjamin Fox

## Requirements

You need to install Spout (http://spout.zeal.co/) Windows only, the same can be done in OSX via Syphon.

Processing 3

Unity 5.6 (although any other version should work)

The Unity project only includes the Oculus plugin, for Vive support you need to import SteamVR and use their camera prefab.
