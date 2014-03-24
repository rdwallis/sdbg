/*
 * Copyright (c) 2012, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.sdbg.debug.core;

import com.github.sdbg.utilities.StringUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * A wrapper class around ILaunchConfiguration and ILaunchConfigurationWorkingCopy objects. It adds
 * compiler type checking to what is essentially a property map.
 */
public class SDBGLaunchConfigWrapper {
  private static final String APPLICATION_ARGUMENTS = "applicationArguments";
  private static final String APPLICATION_NAME = "applicationName";
  private static final String URL_QUERY_PARAMS = "urlQueryParams";

  private static final String SHOW_LAUNCH_OUTPUT = "showLaunchOutput";

  // --enable-experimental-webkit-features and --enable-devtools-experiments
  private static final String ENABLE_EXPERIMENTAL_WEBKIT_FEATURES = "enableExperimentalWebkitFeatures";

  private static final String IS_FILE = "launchFile";

  private static final String DEVICE = "device";

  private static final String DEVICE_REVERSE_FORWARD_PORT = "deviceReverseForwardPort";

  private static final String URL = "url";

  private static final String CONNECTION_HOST = "connectionHost";
  private static final String CONNECTION_PORT = "connectionPort";

  private static final String PROJECT_NAME = "projectName";
  private static final String WORKING_DIRECTORY = "workingDirectory";

  private static final String VM_ARGUMENTS = "vmArguments";

  private ILaunchConfiguration launchConfig;

  /**
   * Create a new DartLaunchConfigWrapper given either a ILaunchConfiguration (for read-only launch
   * configs) or ILaunchConfigurationWorkingCopy (for writeable launch configs).
   */
  public SDBGLaunchConfigWrapper(ILaunchConfiguration launchConfig) {
    this.launchConfig = launchConfig;
  }

  /**
   * Return either the original url, or url + '?' + params.
   * 
   * @param url
   * @return
   */
  public String appendQueryParams(String url) {
    if (getUrlQueryParams().length() > 0) {
      return url + "?" + getUrlQueryParams();
    } else {
      return url;
    }
  }

  /**
   * @return the Dart application file name (e.g. src/HelloWorld.dart)
   */
  public String getApplicationName() {
    try {
      return launchConfig.getAttribute(APPLICATION_NAME, "");
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);

      return "";
    }
  }

  public IResource getApplicationResource() {
    String path = getApplicationName();

    if (path == null || path.length() == 0) {
      return null;
    } else {
      return ResourcesPlugin.getWorkspace().getRoot().findMember(getApplicationName());
    }
  }

  /**
   * @return the arguments string for the Dart application or Browser
   */
  public String getArguments() {
    try {
      return launchConfig.getAttribute(APPLICATION_ARGUMENTS, "");
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);

      return "";
    }
  }

  /**
   * @return the arguments for the Dart application or Browser
   */
  public String[] getArgumentsAsArray() {
    String command = getArguments();

    if (command == null || command.length() == 0) {
      return new String[0];
    }

    return StringUtilities.parseArgumentString(command);
  }

  /**
   * @return the launch configuration that this SDBGLaucnConfigWrapper wraps
   */
  public ILaunchConfiguration getConfig() {
    return launchConfig;
  }

  public String getConnectionHost() {
    try {
      return launchConfig.getAttribute(CONNECTION_HOST, "localhost");
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);
      return "";
    }
  }

  public int getConnectionPort() {
    try {
      return launchConfig.getAttribute(CONNECTION_PORT, 9222);
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);
      return 9222;
    }
  }

  public String getDevice() {
    try {
      return launchConfig.getAttribute(DEVICE, "");
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);

      return "";
    }
  }

  public int getDeviceReverseForwardPort() {
    try {
      return launchConfig.getAttribute(CONNECTION_PORT, 8080);
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);
      return 8080;
    }
  }

  /**
   * @return any configured environment variables
   */
  public Map<String, String> getEnvironment() throws CoreException {
    Map<String, String> map = new HashMap<String, String>();

    for (String envProperty : DebugPlugin.getDefault().getLaunchManager().getEnvironment(
        launchConfig)) {
      int index = envProperty.indexOf('=');
      if (index > 0) {
        String key = envProperty.substring(0, index);
        String value = envProperty.substring(index + 1);
        map.put(key, value);
      }
    }

    return map;
  }

  /**
   * @return the last time this config was launched, or 0 or no such
   */
  public long getLastLaunchTime() {
    // TODO: The persistence of the last launch time should ideally be done outside of the .launch file itself
    // or else the launch file will constantly be dirty, which is annoying when it is stored in a source control prepository
    return 0;
//    try {
//    return 0;
//      String value = launchConfig.getAttribute(LAST_LAUNCH_TIME, "0");
//      return Long.parseLong(value);
//    } catch (NumberFormatException ex) {
//      return 0;
//    } catch (CoreException ce) {
//      SDBGDebugCorePlugin.logError(ce);
//
//      return 0;
//    }
  }

  /**
   * @return the DartProject that contains the application to run
   */
  public IProject getProject() {
    if (getShouldLaunchFile()) {
      IResource resource = getApplicationResource();

      if (resource != null) {
        return resource.getProject();
      }
    }

    String projectName = getProjectName();
    if (projectName.length() > 0) {
      return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
    }

    return null;
  }

  /**
   * @return the name of the DartProject that contains the application to run
   */
  public String getProjectName() {
    try {
      return launchConfig.getAttribute(PROJECT_NAME, "");
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);

      return "";
    }
  }

  public boolean getShouldLaunchFile() {
    try {
      return launchConfig.getAttribute(IS_FILE, true);
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);

      return true;
    }
  }

  public boolean getShowLaunchOutput() {
    try {
      return launchConfig.getAttribute(SHOW_LAUNCH_OUTPUT, false);
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);

      return false;
    }
  }

  public String getUrl() {
    try {
      return launchConfig.getAttribute(URL, "");
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);

      return "";
    }
  }

  /**
   * @return the url query parameters, if any
   */
  public String getUrlQueryParams() {
    try {
      return launchConfig.getAttribute(URL_QUERY_PARAMS, "");
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);

      return "";
    }
  }

  /**
   * @return the arguments string for the Dart VM
   */
  public String getVmArguments() {
    try {
      return launchConfig.getAttribute(VM_ARGUMENTS, "");
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);

      return "";
    }
  }

  /**
   * @return the arguments for the Dart VM
   */
  public String[] getVmArgumentsAsArray() {

    List<String> args = new ArrayList<String>();
    args.addAll(Arrays.asList(StringUtilities.parseArgumentString(getVmArguments())));

    return args.toArray(new String[args.size()]);
  }

  /**
   * @return the cwd for command-line launches
   */
  public String getWorkingDirectory() {
    try {
      return launchConfig.getAttribute(WORKING_DIRECTORY, "");
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);

      return "";
    }
  }

  public boolean isEnableExperimentalWebkitFeatures() {
    try {
      return launchConfig.getAttribute(ENABLE_EXPERIMENTAL_WEBKIT_FEATURES, true);
    } catch (CoreException e) {
      SDBGDebugCorePlugin.logError(e);

      return true;
    }
  }

  /**
   * Indicate that this launch configuration was just launched.
   */
  public void markAsLaunched() {
// TODO: The persistence of the last launch time should ideally be done outside of the .launch file itself
// or else the launch file will constantly be dirty, which is annoying when it is stored in a source control prepository    
//    try {
//      ILaunchConfigurationWorkingCopy workingCopy = launchConfig.getWorkingCopy();
//
//      long launchTime = System.currentTimeMillis();
//
//      workingCopy.setAttribute(LAST_LAUNCH_TIME, Long.toString(launchTime));
//
//      workingCopy.doSave();
//    } catch (CoreException ce) {
//      SDBGDebugCorePlugin.logError(ce);
//    }
  }

  /**
   * @see #getApplicationName()
   */
  public void setApplicationName(String value) {
    getWorkingCopy().setAttribute(APPLICATION_NAME, value);

    updateMappedResources(value);
  }

  /**
   * @see #getArguments()
   */
  public void setArguments(String value) {
    getWorkingCopy().setAttribute(APPLICATION_ARGUMENTS, value);
  }

  /**
   * @see #getConnectionHost()
   */
  public void setConnectionHost(String value) {
    getWorkingCopy().setAttribute(CONNECTION_HOST, value);
  }

  /**
   * @see #getConnectionPort()
   */
  public void setConnectionPort(int value) {
    getWorkingCopy().setAttribute(CONNECTION_PORT, value);
  }

  /**
   * @see #getDevice()
   */
  public void setDevice(String value) {
    getWorkingCopy().setAttribute(DEVICE, value);
  }

  /**
   * @see #getDeviceReverseForwardPort()
   */
  public void setDeviceReverseForwardPort(int value) {
    getWorkingCopy().setAttribute(DEVICE_REVERSE_FORWARD_PORT, value);
  }

  /**
   * @see #getProjectName()
   */
  public void setProjectName(String value) {
    getWorkingCopy().setAttribute(PROJECT_NAME, value);

    if (getApplicationResource() == null) {
      updateMappedResources(value);
    }
  }

  public void setShouldLaunchFile(boolean value) {
    getWorkingCopy().setAttribute(IS_FILE, value);
  }

  public void setShowLaunchOutput(boolean value) {
    getWorkingCopy().setAttribute(SHOW_LAUNCH_OUTPUT, value);
  }

  /**
   * @see #getUrl()
   */
  public void setUrl(String value) {
    getWorkingCopy().setAttribute(URL, value);
  }

  /**
   * @see #getUrlQueryParams()()
   */
  public void setUrlQueryParams(String value) {
    getWorkingCopy().setAttribute(URL_QUERY_PARAMS, value);
  }

  public void setUseWebComponents(boolean value) {
    getWorkingCopy().setAttribute(ENABLE_EXPERIMENTAL_WEBKIT_FEATURES, value);
  }

  /**
   * @see #getVmArguments()
   */
  public void setVmArguments(String value) {
    getWorkingCopy().setAttribute(VM_ARGUMENTS, value);
  }

  public void setWorkingDirectory(String value) {
    getWorkingCopy().setAttribute(WORKING_DIRECTORY, value);
  }

  protected ILaunchConfigurationWorkingCopy getWorkingCopy() {
    return (ILaunchConfigurationWorkingCopy) launchConfig;
  }

  private void updateMappedResources(String resourcePath) {
    IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(resourcePath);

    if (resource != null && !(resource instanceof IWorkspaceRoot)) {
      getWorkingCopy().setMappedResources(new IResource[] {resource});
    } else {
      getWorkingCopy().setMappedResources(null);
    }
  }
}
