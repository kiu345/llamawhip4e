package com.github.kiu345.eclipse.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;

public class MockUtils {
    public static ILog createLogMock() {
        return new ILog() {
            @Override
            public void removeLogListener(ILogListener listener) {
                // FAKE NOOP
            }

            @Override
            public void log(IStatus status) {
                System.out.println(status.getMessage());
            }

            @Override
            public Bundle getBundle() {
                // FAKE NOOP
                return new Bundle() {

                    @Override
                    public int compareTo(Bundle o) {
                        return 0;
                    }

                    @Override
                    public void update(InputStream input) throws BundleException {
                        // FAKE NOOP
                    }

                    @Override
                    public void update() throws BundleException {
                        // FAKE NOOP

                    }

                    @Override
                    public void uninstall() throws BundleException {
                        // FAKE NOOP

                    }

                    @Override
                    public void stop(int options) throws BundleException {
                        // FAKE NOOP
                    }

                    @Override
                    public void stop() throws BundleException {
                        // FAKE NOOP

                    }

                    @Override
                    public void start(int options) throws BundleException {
                        // FAKE NOOP

                    }

                    @Override
                    public void start() throws BundleException {
                        // FAKE NOOP
                    }

                    @Override
                    public Class<?> loadClass(String name) throws ClassNotFoundException {
                        return null;
                    }

                    @Override
                    public boolean hasPermission(Object permission) {
                        return false;
                    }

                    @Override
                    public Version getVersion() {
                        return null;
                    }

                    @Override
                    public String getSymbolicName() {
                        return "test";
                    }

                    @Override
                    public int getState() {
                        // FAKE NOOP
                        return 0;
                    }

                    @Override
                    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public ServiceReference<?>[] getServicesInUse() {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public Enumeration<URL> getResources(String name) throws IOException {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public URL getResource(String name) {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public ServiceReference<?>[] getRegisteredServices() {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public String getLocation() {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public long getLastModified() {
                        // FAKE NOOP
                        return 0;
                    }

                    @Override
                    public Dictionary<String, String> getHeaders(String locale) {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public Dictionary<String, String> getHeaders() {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public Enumeration<String> getEntryPaths(String path) {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public URL getEntry(String path) {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public File getDataFile(String filename) {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public long getBundleId() {
                        // FAKE NOOP
                        return 0;
                    }

                    @Override
                    public BundleContext getBundleContext() {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
                        // FAKE NOOP
                        return null;
                    }

                    @Override
                    public <A> A adapt(Class<A> type) {
                        // FAKE NOOP
                        return null;
                    }
                };
            }

            @Override
            public void addLogListener(ILogListener listener) {
                // FAKE NOOP
            }
        };
    }

    public static IWorkspace createIDEEnv(Class<?> context) throws CoreException, IOException {
        BundleContext bundleContext = FrameworkUtil.getBundle(context).getBundleContext();
        ServiceTracker<IWorkspace, IWorkspace> workspaceTracker = new ServiceTracker<>(bundleContext, IWorkspace.class, null);

        workspaceTracker.open();
        IWorkspace workspace = workspaceTracker.getService();
        IWorkspaceRoot root = workspace.getRoot();

        // Create a project
        IProject project = root.getProject("Test Project");
        if (!project.exists()) {
            IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
            desc.setNatureIds(new String[] { JavaCore.NATURE_ID }); // set Java nature
            project.create(desc, null);
        }
        if (!project.isOpen()) {
            project.open(null);
        }
        // add javadoc location for JDK
        IJavaProject javaProject = JavaCore.create(project);

        IPath jrePath = new Path(JavaRuntime.JRE_CONTAINER);
//        IClasspathEntry jreEntry = JavaCore.newContainerEntry(jrePath);

        IPath pathToDoc = new Path("https://docs.oracle.com/en/java/javase/21/docs/api/");

        IClasspathAttribute[] extraAttributes = new IClasspathAttribute[] {
                JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, pathToDoc.toString())
        };

        IClasspathEntry newJreEntry = JavaCore.newContainerEntry(jrePath, null, extraAttributes, false);

        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        List<IClasspathEntry> newEntries = new ArrayList<>(Arrays.asList(oldEntries));

        boolean foundJRE = false;
        for (int i = 0; i < oldEntries.length; i++) {
            if (oldEntries[i].getPath().equals(jrePath)) {
                newEntries.set(i, newJreEntry);
                foundJRE = true;
                break;
            }
        }
        if (!foundJRE) {
            newEntries.add(newJreEntry); // If the JRE entry was not found, add it.
        }
        javaProject.setRawClasspath(newEntries.toArray(new IClasspathEntry[0]), null);

        // Create a folder
        IFolder srcFolder = project.getFolder("src");
        if (!srcFolder.exists()) {
            srcFolder.create(IResource.NONE, true, null);
        }

        // Create a folder for the package
        IFolder packageFolder = srcFolder.getFolder("com");
        if (!packageFolder.exists()) {
            packageFolder.create(IResource.NONE, true, null);
        }
        packageFolder = packageFolder.getFolder("example");
        if (!packageFolder.exists()) {
            packageFolder.create(IResource.NONE, true, null);
        }
        // Create a file
        IFile file = packageFolder.getFile("Test.java");
        if (!file.exists()) {

            String classBody = """
                    package src.com.example;
                    /**
                     * Class comment
                     */
                    public class Test
                    {
                        /**
                         * Method returns 1
                         */
                        public int testMethod()
                        {
                            return 1;
                        }
                        /**
                         * Method returns 2
                         */
                        public int testMethod2()
                        {
                            return 2;
                        }
                    }
                    """;

            try (ByteArrayInputStream source = new ByteArrayInputStream(classBody.getBytes())) {
                file.create(source, IResource.NONE, null);
            }
        }
        return workspace;
    }

}
