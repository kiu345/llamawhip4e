package com.github.kiu345.eclipse.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Utility class providing mock objects for testing Eclipse-based applications.
 * Includes helpers to create mock logs, bundles, and an IDE environment.
 */
public class MockUtils {
    public static ILog createLogMock() {
        ILog logMock = Mockito.mock(ILog.class);

        when(logMock.getBundle()).thenReturn(createBundle());

        doAnswer((e) -> {
            System.out.println((String) e.getArgument(0));
            return null;
        }).when(logMock).log(any(IStatus.class));

        doAnswer((e) -> {
            System.out.println((String) e.getArgument(0));
            return null;
        }).when(logMock).info(anyString());
        doAnswer((e) -> {
            System.out.println((String) e.getArgument(0));
            return null;
        }).when(logMock).info(anyString(), any(Throwable.class));

        doAnswer((e) -> {
            System.out.println((String) e.getArgument(0));
            return null;
        }).when(logMock).warn(anyString());
        doAnswer((e) -> {
            System.out.println((String) e.getArgument(0));
            return null;
        }).when(logMock).warn(anyString(), any(Throwable.class));

        doAnswer((e) -> {
            System.err.println((String) e.getArgument(0));
            return null;
        }).when(logMock).error(anyString());
        doAnswer((e) -> {
            System.err.println((String) e.getArgument(0));
            return null;
        }).when(logMock).error(anyString(), any(Throwable.class));

        return logMock;
    }

    public static Bundle createBundle() {
        Bundle mockBundle = Mockito.mock(Bundle.class);
//        when(mockBundle.getBundleId()).thenReturn(1234567890l);
//        when(mockBundle.getVersion()).thenReturn(Version.emptyVersion);
//        when(mockBundle.getSymbolicName()).thenReturn("mockbundle");

        return mockBundle;
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
