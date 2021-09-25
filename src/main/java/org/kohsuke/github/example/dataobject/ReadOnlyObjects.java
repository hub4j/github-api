package org.kohsuke.github.example.dataobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * {@link org.kohsuke.github.GHMeta} wraps the list of GitHub's IP addresses.
 * <p>
 * This class is used to show examples of different ways to create simple read-only data objects. For data objects that
 * can be modified, perform actions, or get other objects we'll need other examples.
 * <p>
 * IMPORTANT: There is no one right way to do this, but there are better and worse.
 * <ul>
 * <li>Better: {@link GHMetaGettersUnmodifiable} is a good balance of clarity and brevity</li>
 * <li>Worse: {@link GHMetaPublic} exposes setters that are not needed, making it unclear that fields are actually
 * read-only</li>
 * </ul>
 *
 * @author Liam Newman
 * @see org.kohsuke.github.GHMeta
 * @see <a href="https://developer.github.com/v3/meta/#meta">Get Meta</a>
 */
public final class ReadOnlyObjects {

    /**
     * All GHMeta data objects should expose these values.
     *
     * @author Liam Newman
     */
    public interface GHMetaExample {
        /**
         * Is verifiable password authentication boolean.
         *
         * @return the boolean
         */
        boolean isVerifiablePasswordAuthentication();

        /**
         * Gets hooks.
         *
         * @return the hooks
         */
        List<String> getHooks();

        /**
         * Gets git.
         *
         * @return the git
         */
        List<String> getGit();

        /**
         * Gets web.
         *
         * @return the web
         */
        List<String> getWeb();

        /**
         * Gets api.
         *
         * @return the api
         */
        List<String> getApi();

        /**
         * Gets pages.
         *
         * @return the pages
         */
        List<String> getPages();

        /**
         * Gets importer.
         *
         * @return the importer
         */
        List<String> getImporter();
    }

    /**
     * This version uses public getters and setters and leaves it up to Jackson how it wants to fill them.
     * <p>
     * Pro:
     * <ul>
     * <li>Easy to create</li>
     * <li>Not much code</li>
     * <li>Mininal annotations</li>
     * </ul>
     * Con:
     * <ul>
     * <li>Exposes public setters for fields that should not be changed, flagged by spotbugs</li>
     * <li>Lists modifiable when they should not be changed</li>
     * <li>Jackson generally doesn't call the setters, it just sets the fields directly</li>
     * </ul>
     *
     * @author Paulo Miguel Almeida
     * @see org.kohsuke.github.GHMeta
     */
    public static class GHMetaPublic implements GHMetaExample {

        @JsonProperty("verifiable_password_authentication")
        private boolean verifiablePasswordAuthentication;
        private List<String> hooks;
        private List<String> git;
        private List<String> web;
        private List<String> api;
        private List<String> pages;
        private List<String> importer;

        public boolean isVerifiablePasswordAuthentication() {
            return verifiablePasswordAuthentication;
        }

        /**
         * Sets verifiable password authentication.
         *
         * @param verifiablePasswordAuthentication
         *            the verifiable password authentication
         */
        public void setVerifiablePasswordAuthentication(boolean verifiablePasswordAuthentication) {
            this.verifiablePasswordAuthentication = verifiablePasswordAuthentication;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getHooks() {
            return hooks;
        }

        /**
         * Sets hooks.
         *
         * @param hooks
         *            the hooks
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Spotbugs also doesn't like this")
        public void setHooks(List<String> hooks) {
            this.hooks = hooks;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getGit() {
            return git;
        }

        /**
         * Sets git.
         *
         * @param git
         *            the git
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Spotbugs also doesn't like this")
        public void setGit(List<String> git) {
            this.git = git;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getWeb() {
            return web;
        }

        /**
         * Sets web.
         *
         * @param web
         *            the web
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Spotbugs also doesn't like this")
        public void setWeb(List<String> web) {
            this.web = web;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getApi() {
            return api;
        }

        /**
         * Sets api.
         *
         * @param api
         *            the api
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Spotbugs also doesn't like this")
        public void setApi(List<String> api) {
            this.api = api;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getPages() {
            return pages;
        }

        /**
         * Sets pages.
         *
         * @param pages
         *            the pages
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Spotbugs also doesn't like this")
        public void setPages(List<String> pages) {
            this.pages = pages;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getImporter() {
            return importer;
        }

        /**
         * Sets importer.
         *
         * @param importer
         *            the importer
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP2" }, justification = "Spotbugs also doesn't like this")
        public void setImporter(List<String> importer) {
            this.importer = importer;
        }

    }

    /**
     * This version uses public getters and shows that package or private setters both can be used by jackson. You can
     * check this by running in debug and setting break points in the setters.
     *
     * <p>
     * Pro:
     * <ul>
     * <li>Easy to create</li>
     * <li>Not much code</li>
     * <li>Some annotations</li>
     * </ul>
     * Con:
     * <ul>
     * <li>Exposes some package setters for fields that should not be changed, better than public</li>
     * <li>Lists modifiable when they should not be changed</li>
     * </ul>
     *
     * @author Liam Newman
     * @see org.kohsuke.github.GHMeta
     */
    public static class GHMetaPackage implements GHMetaExample {

        private boolean verifiablePasswordAuthentication;
        private List<String> hooks;
        private List<String> git;
        private List<String> web;
        private List<String> api;
        private List<String> pages;

        /**
         * Missing {@link JsonProperty} or having it on the field will cause Jackson to ignore getters and setters.
         */
        @JsonProperty
        private List<String> importer;

        @JsonProperty("verifiable_password_authentication")
        public boolean isVerifiablePasswordAuthentication() {
            return verifiablePasswordAuthentication;
        }

        private void setVerifiablePasswordAuthentication(boolean verifiablePasswordAuthentication) {
            this.verifiablePasswordAuthentication = verifiablePasswordAuthentication;
        }

        @JsonProperty
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getHooks() {
            return hooks;
        }

        /**
         * Setters can be private (or package local) and will still be called by Jackson. The {@link JsonProperty} can
         * got on the getter or setter and still work.
         *
         * @param hooks
         *            list of hooks
         */
        private void setHooks(List<String> hooks) {
            this.hooks = hooks;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getGit() {
            return git;
        }

        /**
         * Since we mostly use Jackson for deserialization, {@link JsonSetter} is also okay, but {@link JsonProperty} is
         * preferred.
         *
         * @param git
         *            list of git addresses
         */
        @JsonSetter
        void setGit(List<String> git) {
            this.git = git;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getWeb() {
            return web;
        }

        /**
         * The {@link JsonProperty} can got on the getter or setter and still work.
         *
         * @param web
         *            list of web addresses
         */
        void setWeb(List<String> web) {
            this.web = web;
        }

        @JsonProperty
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getApi() {
            return api;
        }

        void setApi(List<String> api) {
            this.api = api;
        }

        @JsonProperty
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getPages() {
            return pages;
        }

        void setPages(List<String> pages) {
            this.pages = pages;
        }

        /**
         * Missing {@link JsonProperty} or having it on the field will cause Jackson to ignore getters and setters.
         *
         * @return list of importer addresses
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Noted above")
        public List<String> getImporter() {
            return importer;
        }

        /**
         * Missing {@link JsonProperty} or having it on the field will cause Jackson to ignore getters and setters.
         *
         * @param importer
         *            list of importer addresses
         */
        void setImporter(List<String> importer) {
            this.importer = importer;
        }

    }

    /**
     * This version uses only public getters and returns unmodifiable lists.
     *
     *
     * <p>
     * Pro:
     * <ul>
     * <li>Very Easy to create</li>
     * <li>Minimal code</li>
     * <li>Mininal annotations</li>
     * <li>Fields effectively final and lists unmodifiable</li>
     * </ul>
     * Con:
     * <ul>
     * <li>Effectively final is not quite really final</li>
     * <li>If one of the lists were missing (an option member, for example), it will throw NPE but we could mitigate by
     * checking for null or assigning a default.</li>
     * </ul>
     *
     * @author Liam Newman
     * @see org.kohsuke.github.GHMeta
     */
    public static class GHMetaGettersUnmodifiable implements GHMetaExample {

        @JsonProperty("verifiable_password_authentication")
        private boolean verifiablePasswordAuthentication;
        private List<String> hooks;
        private List<String> git;
        private List<String> web;
        private List<String> api;
        private List<String> pages;
        /**
         * If this were an optional member, we could fill it with an empty list by default.
         */
        private List<String> importer = new ArrayList<>();

        public boolean isVerifiablePasswordAuthentication() {
            return verifiablePasswordAuthentication;
        }

        public List<String> getHooks() {
            return Collections.unmodifiableList(hooks);
        }

        public List<String> getGit() {
            return Collections.unmodifiableList(git);
        }

        public List<String> getWeb() {
            return Collections.unmodifiableList(web);
        }

        public List<String> getApi() {
            return Collections.unmodifiableList(api);
        }

        public List<String> getPages() {
            return Collections.unmodifiableList(pages);
        }

        public List<String> getImporter() {
            return Collections.unmodifiableList(importer);
        }
    }

    /**
     * This version uses only public getters and returns unmodifiable lists and has final fields
     * <p>
     * Pro:
     * <ul>
     * <li>Moderate amount of code</li>
     * <li>More annotations</li>
     * <li>Fields final and lists unmodifiable</li>
     * </ul>
     * Con:
     * <ul>
     * <li>Extra allocations - default array lists will be replaced by Jackson (yes, even though they are final)</li>
     * <li>Added constructor is annoying</li>
     * <li>If this object could be refreshed or populated, then the final is misleading (and possibly buggy)</li>
     * </ul>
     *
     * @author Liam Newman
     * @see org.kohsuke.github.GHMeta
     */
    public static class GHMetaGettersFinal implements GHMetaExample {

        private final boolean verifiablePasswordAuthentication;
        private final List<String> hooks = new ArrayList<>();
        private final List<String> git = new ArrayList<>();
        private final List<String> web = new ArrayList<>();
        private final List<String> api = new ArrayList<>();
        private final List<String> pages = new ArrayList<>();
        private final List<String> importer = new ArrayList<>();

        @JsonCreator
        private GHMetaGettersFinal(
                @JsonProperty("verifiable_password_authentication") boolean verifiablePasswordAuthentication) {
            // boolean fields when final seem to be really final, so we have to switch to constructor
            this.verifiablePasswordAuthentication = verifiablePasswordAuthentication;
        }

        public boolean isVerifiablePasswordAuthentication() {
            return verifiablePasswordAuthentication;
        }

        public List<String> getHooks() {
            return Collections.unmodifiableList(hooks);
        }

        public List<String> getGit() {
            return Collections.unmodifiableList(git);
        }

        public List<String> getWeb() {
            return Collections.unmodifiableList(web);
        }

        public List<String> getApi() {
            return Collections.unmodifiableList(api);
        }

        public List<String> getPages() {
            return Collections.unmodifiableList(pages);
        }

        public List<String> getImporter() {
            return Collections.unmodifiableList(importer);
        }
    }

    /**
     * This version uses only public getters and returns unmodifiable lists
     * <p>
     * Pro:
     * <ul>
     * <li>Fields final and lists unmodifiable</li>
     * <li>Construction behavior can be controlled - if values depended on each other or needed to be set in a specific
     * order, this could do that.</li>
     * <li>JsonProrperty "required" works on JsonCreator constructors - lets annotation define required values</li>
     * </ul>
     * Con:
     * <ul>
     * <li>There is no way you'd know about this without some research</li>
     * <li>Specific annotations needed</li>
     * <li>Nonnull annotations are misleading - null value is not checked even for "required" constructor
     * parameters</li>
     * <li>Brittle and verbose - not friendly to large number of fields</li>
     * </ul>
     *
     * @author Liam Newman
     * @see org.kohsuke.github.GHMeta
     */
    public static class GHMetaGettersFinalCreator implements GHMetaExample {

        private final boolean verifiablePasswordAuthentication;
        private final List<String> hooks;
        private final List<String> git;
        private final List<String> web;
        private final List<String> api;
        private final List<String> pages;
        private final List<String> importer;

        /**
         *
         * @param hooks
         *            the hooks - required property works, but only on creator json properties like this, ignores
         *            Nonnull, checked manually
         * @param git
         *            the git list - required property works, but only on creator json properties like this, misleading
         *            Nonnull annotation
         * @param web
         *            the web list - misleading Nonnull annotation
         * @param api
         *            the api list - misleading Nonnull annotation
         * @param pages
         *            the pages list - misleading Nonnull annotation
         * @param importer
         *            the importer list - misleading Nonnull annotation
         * @param verifiablePasswordAuthentication
         *            true or false
         */
        @JsonCreator
        private GHMetaGettersFinalCreator(@Nonnull @JsonProperty(value = "hooks", required = true) List<String> hooks,
                @Nonnull @JsonProperty(value = "git", required = true) List<String> git,
                @Nonnull @JsonProperty("web") List<String> web,
                @Nonnull @JsonProperty("api") List<String> api,
                @Nonnull @JsonProperty("pages") List<String> pages,
                @Nonnull @JsonProperty("importer") List<String> importer,
                @JsonProperty("verifiable_password_authentication") boolean verifiablePasswordAuthentication) {

            // to ensure a value is actually not null we still have to do a null check
            Objects.requireNonNull(hooks);

            this.verifiablePasswordAuthentication = verifiablePasswordAuthentication;
            this.hooks = Collections.unmodifiableList(hooks);
            this.git = Collections.unmodifiableList(git);
            this.web = Collections.unmodifiableList(web);
            this.api = Collections.unmodifiableList(api);
            this.pages = Collections.unmodifiableList(pages);
            this.importer = Collections.unmodifiableList(importer);
        }

        public boolean isVerifiablePasswordAuthentication() {
            return verifiablePasswordAuthentication;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Unmodifiable but spotbugs doesn't detect")
        public List<String> getHooks() {
            return hooks;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Unmodifiable but spotbugs doesn't detect")
        public List<String> getGit() {
            return git;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Unmodifiable but spotbugs doesn't detect")
        public List<String> getWeb() {
            return web;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Unmodifiable but spotbugs doesn't detect")
        public List<String> getApi() {
            return api;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Unmodifiable but spotbugs doesn't detect")
        public List<String> getPages() {
            return pages;
        }

        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Unmodifiable but spotbugs doesn't detect")
        public List<String> getImporter() {
            return importer;
        }
    }
}
