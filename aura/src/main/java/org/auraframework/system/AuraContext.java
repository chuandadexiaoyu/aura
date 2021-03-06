/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.system;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.auraframework.def.BaseComponentDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.DefDescriptor.DefType;
import org.auraframework.instance.Action;
import org.auraframework.instance.BaseComponent;
import org.auraframework.instance.Event;
import org.auraframework.instance.GlobalValueProvider;
import org.auraframework.instance.InstanceStack;
import org.auraframework.instance.ValueProviderType;
import org.auraframework.util.javascript.directive.JavascriptGeneratorMode;
import org.auraframework.util.json.JsonSerializationContext;

/**
 * AuraContext public interface
 */
public interface AuraContext {

    public static enum Mode {

        DEV(false, true, true, JavascriptGeneratorMode.DEVELOPMENT, true),
        STATS(true, false, true, JavascriptGeneratorMode.STATS, true),
        UTEST(true, false, true, JavascriptGeneratorMode.PRODUCTION, true),
        FTEST(true, false, true, JavascriptGeneratorMode.TESTING, true),
        JSTEST(true, false, false, JavascriptGeneratorMode.TESTING, false),
        AUTOJSTEST(true, false, false, JavascriptGeneratorMode.AUTOTESTING, false),
        JSTESTDEBUG(true, false, true, JavascriptGeneratorMode.TESTINGDEBUG, false),
        AUTOJSTESTDEBUG(true, false, true, JavascriptGeneratorMode.AUTOTESTINGDEBUG, false),
        PTEST(false, false, false, JavascriptGeneratorMode.PTEST, true),
        CADENCE(false, false, false, JavascriptGeneratorMode.PTEST, true),
        PRODDEBUG(false, false, false, JavascriptGeneratorMode.PRODUCTIONDEBUG, true),
        PROD(false, false, false, JavascriptGeneratorMode.PRODUCTION, true),
        SELENIUM(true, false, true, JavascriptGeneratorMode.AUTOTESTING, true),
        SELENIUMDEBUG(true, false, true, JavascriptGeneratorMode.AUTOTESTINGDEBUG, true);

        private final JavascriptGeneratorMode javascriptMode;
        private final boolean isTestMode;
        private final boolean isDevMode;
        private final boolean prettyPrint;
        private final boolean allowLocalRendering;

        private Mode(boolean isTestMode, boolean isDevMode, boolean prettyPrint, JavascriptGeneratorMode jsMode,
                boolean allowLocalRendering) {
            this.isTestMode = isTestMode;
            this.isDevMode = isDevMode;
            this.javascriptMode = jsMode;
            this.prettyPrint = prettyPrint;
            this.allowLocalRendering = allowLocalRendering;
        }

        public boolean isTestMode() {
            return isTestMode;
        }

        public boolean isDevMode() {
            return isDevMode;
        }

        public boolean prettyPrint() {
            return prettyPrint;
        }

        public boolean allowLocalRendering() {
            return allowLocalRendering;
        }

        /**
         * @return Returns the jsMode.
         */
        public JavascriptGeneratorMode getJavascriptMode() {
            return javascriptMode;
        }
    }

    public static boolean isDebugToolEnabled = false;
    
    public static enum Format {
        MANIFEST, CSS, JS, JSON, HTML;
    }

    public static enum Access {
        PUBLIC, AUTHENTICATED
    }

    /**
     * @return the master def registry
     */
    MasterDefRegistry getDefRegistry();

    /**
     * TODO: should have serialization contexts for any format, this shouldn't
     * be tied to json
     *
     * @return the json serialization context to use
     */
    JsonSerializationContext getJsonSerializationContext();

    /**
     * @return whether the descriptor has been checked for freshness in this
     *         thread
     */
    boolean hasChecked(DefDescriptor<?> d);

    /**
     * Sets that the given descriptor was checked for freshness, and so
     * shouldn't be checked again
     *
     * @param d descriptor that was checked
     */
    void setStaleCheck(DefDescriptor<?> d);

    /**
     * @return the current component being processed in the tree (for use by
     *         controllers and models)
     */
    BaseComponent<?, ?> getCurrentComponent();

    /**
     * Set the current component, so that the components controller can access
     * it.
     *
     * TODO: what is this for.
     * TODO: this is not handled as a stack, so it is almost certainly broken.
     *
     * @param nextComponent The component to set.
     * @return the previous component
     */
    BaseComponent<?, ?> setCurrentComponent(BaseComponent<?, ?> nextComponent);

    /**
     * Get the currently processing action.
     *
     * @return the current action being processed (for use by controllers)
     */
    Action getCurrentAction();

    /**
     * Set the current action, so that the components controller can access it
     *
     * @param nextAction
     * @return the previous action
     */
    Action setCurrentAction(Action nextAction);

    /**
     * Set the current namespace.
     *
     * FIXME: this is an anti-pattern. it is used inside calls to set the
     * current namespace, but is never reset, so it persists in strange and
     * interesting ways. Figure out another way to do this?
     */
    void setCurrentNamespace(String namespace);

    /**
     * Get the current namespace.
     */
    String getCurrentNamespace();

    /**
     * If a qualifiedName for a DefDescriptor of the given type does not include
     * a prefix (apex:// or java://, etc...), this method on the context will be
     * consulted to find out what the default prefix for the given DefType is.
     *
     * @param defType
     * @return The default prefix for the given DefType in this context
     */
    String getDefaultPrefix(DefType defType);

    /**
     * Get the mode of execution.
     *
     * This should be consistent across the entire request.
     */
    Mode getMode();

    /**
     * Shortcut to check if we are running in one of several testing modes. Use {@link #getMode()} to get the specific
     * mode.
     */
    boolean isTestMode();

    /**
     * Set the 'number' of this context.
     *
     * This is used in component ids to guarantee that each global id is unique.
     * This is passed in from the client, and should never be set outside of
     * Aura code during normal operation.
     *
     * @param num The 'number' to use as an ID for this context.
     */
    void setNum(String num);

    /**
     * Get the context 'number'.
     */
    String getNum();

    /**
     * Namespaces whose defs should be, or have been preloaded on the client.
     *
     * @param preload
     */
    void addPreload(String preload);

    /**
     * get the current set of preloads.
     *
     * By default, the aura and os namespaces are included.
     */
    Set<String> getPreloads();

    Format getFormat();

    Access getAccess();

    Map<ValueProviderType, GlobalValueProvider> getGlobalProviders();

    Map<String, BaseComponent<?, ?>> getComponents();

    void registerComponent(BaseComponent<?, ?> component);

    int getNextId();

    String getContextPath();

    void setContextPath(String path);

    boolean getSerializePreLoad();

    boolean getSerializeLastMod();

    void setSerializeLastMod(boolean serializeLastMod);

    boolean isPreloading();

    void setPreloading(boolean p);

    void addDynamicNamespace(String namespace);

    /**
     * Set the incoming loaded descriptors.
     *
     * @param clientLoaded the set of loaded descriptors from the client.
     */
    void setClientLoaded(Map<DefDescriptor<?>,String> clientLoaded);

    /**
     * Get the set of descriptors loaded on the client, and sent in the request.
     *
     * @return a map of descriptor to UID, unmodifiable.
     */
    Map<DefDescriptor<?>,String> getClientLoaded();

    /**
     * Add a loaded descriptor+UID pair.
     *
     * This routine will remember a descriptor in the set of loaded
     * descriptors along with a uid for validating the load (and
     * 'timestamping' it). This should be used with care, as it will
     * be serialized with every request, so size should be a
     * consideration.
     *
     * @param descriptor The loaded descriptor.
     * @param uid the UID that was loaded.
     */
    void addLoaded(DefDescriptor<?> descriptor, String uid);

    /**
     * Drop a component from the set of loaded components.
     *
     * Sober up our set. This can be used to remove a descriptor
     * that is already covered by the set of loaded components.
     *
     * @param descriptor the previously marked 'loaded' descriptor.
     */
    public void dropLoaded(DefDescriptor<?> descriptor);

    /**
     * Get the uid string for a descriptor.
     *
     * @param descriptor the descriptor that we need a UID for.
     * @return the uid from the request (null if none).
     */
    public String getUid(DefDescriptor<?> descriptor);

    /**
     * Get the set of loaded descriptors with the uid.
     *
     * This set of descriptors should be the complete set of loaded
     * descriptors that we choose to remember. Things outside of the
     * dependency set will be resent.
     *
     * @return the map of descriptors to UIDs, UIDs are allowed to be null
     */
    Map<DefDescriptor<?>, String> getLoaded();

    /**
     * Check if a descriptor has been preloaded.
     */
    boolean isPreloaded(DefDescriptor<?> descriptor);

    /**
     * Get the application (or component) descriptor.
     *
     * This returns the currently loaded application/component for this context.
     * It can only be a component for non-production mode.
     *
     * @return the component or application (should rarely be null).
     */
    DefDescriptor<? extends BaseComponentDef> getApplicationDescriptor();

    /**
     * Set the application (or component) descriptor.
     *
     * This sets the application. It should generally be used at context start time
     * only, and will only allow certain overrides.
     *
     * @param appDesc the descriptor for the application/component.
     */
    void setApplicationDescriptor(DefDescriptor<? extends BaseComponentDef> appDesc);

    /**
     * Get the current 'loading' application descriptor.
     *
     * This generally returns the application descriptor passed in from the client, but
     * in dev mode, when a quick fix exception occurs, this will be the quick fix rather
     * than the application. That way we keep our context clean, but remember that we
     * have a quick fix.
     *
     * @return the application descriptor.
     */
    DefDescriptor<? extends BaseComponentDef> getLoadingApplicationDescriptor();

    /**
     * Set the loading application (or component) descriptor.
     *
     * This sets a descriptor to tell the app server that we are actually loading a different
     * application/component than the original one supplied. This is used to override the
     * descriptor in the case of a quick fix (but could be used for other things as well).
     *
     * @param loadingAppDesc the descriptor for the application/component.
     */
    void setLoadingApplicationDescriptor(DefDescriptor<? extends BaseComponentDef> loadingAppDesc);

    /**
     * Set the definitions that the client should already have.
     *
     * @param preloaded the actual set.
     */
    void setPreloadedDefinitions(Set<DefDescriptor<?>> preloaded);

    /**
     * Get the definitions that the client should already have.
     *
     * @return the actual set (unmodifiable).
     */
    Set<DefDescriptor<?>> getPreloadedDefinitions();

    List<Locale> getRequestedLocales();

    void setRequestedLocales(List<Locale> requestedLocales);

    Client getClient();

    void setClient(Client client);

    String getLastMod();

    void setLastMod(String lastMod);

    /**
     *
     * @param event - Instance of the {@link org.auraframework.instance.Event} to be fired at the client.
     * @throws Exception - If the {@link org.auraframework.def.EventType} is not
     *             APPLICATION or Event object's definition cannot be found.
     */
    void addClientApplicationEvent(Event event) throws Exception;

    List<Event> getClientEvents();

    boolean isDevMode();

    /**
     * Set the framework UID from the client (or server).
     *
     * @param uid UID that we should set.
     */
    void setFrameworkUID(String uid);

    /**
     * Get the framework UID.
     *
     * @return the context's idea of the UID.
     */
    String getFrameworkUID();

    /**
     * FIXME: This is busted!!!!
     */
    boolean getIsDebugToolEnabled();

    InstanceStack getInstanceStack();
}
