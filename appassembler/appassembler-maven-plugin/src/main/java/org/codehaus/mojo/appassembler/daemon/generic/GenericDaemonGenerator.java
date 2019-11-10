package org.codehaus.mojo.appassembler.daemon.generic;

/*
 * The MIT License
 *
 * Copyright (c) 2006-2012, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerationRequest;
import org.codehaus.mojo.appassembler.daemon.DaemonGenerator;
import org.codehaus.mojo.appassembler.daemon.DaemonGeneratorException;
import org.codehaus.mojo.appassembler.daemon.merge.DaemonMerger;
import org.codehaus.mojo.appassembler.model.Classpath;
import org.codehaus.mojo.appassembler.model.Daemon;
import org.codehaus.mojo.appassembler.model.Dependency;
import org.codehaus.mojo.appassembler.model.io.stax.AppassemblerModelStaxWriter;
import org.codehaus.mojo.appassembler.util.DependencyFactory;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @plexus.component role-hint="generic"
 */
public class GenericDaemonGenerator
    extends AbstractLogEnabled
    implements DaemonGenerator
{
    /**
     * @plexus.requirement
     */
    private DaemonMerger daemonMerger;

    // -----------------------------------------------------------------------
    // DaemonGenerator Implementation
    // -----------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.codehaus.mojo.appassembler.daemon.DaemonGenerator#generate(org.codehaus.mojo.appassembler.daemon.
     * DaemonGenerationRequest)
     */
    public void generate( DaemonGenerationRequest request )
        throws DaemonGeneratorException
    {
        // -----------------------------------------------------------------------
        // Create the daemon from the Maven project
        // -----------------------------------------------------------------------

        Daemon createdDaemon =
            createDaemon( request.getMavenProject(), request.getRepositoryLayout(), request.getOutputFileNameMapping() );

        // -----------------------------------------------------------------------
        // Merge the given stub daemon and the generated
        // -----------------------------------------------------------------------

        Daemon mergedDaemon = daemonMerger.mergeDaemons( request.getDaemon(), createdDaemon );

        // -----------------------------------------------------------------------
        // Write out the project
        // -----------------------------------------------------------------------

        OutputStreamWriter writer = null;

        try
        {

            FileUtils.forceMkdir( request.getOutputDirectory() );

            File outputFile = new File( request.getOutputDirectory(), mergedDaemon.getId() + ".xml" );

            FileOutputStream fos = new FileOutputStream( outputFile );

            writer = new OutputStreamWriter( fos, "UTF-8" );

            AppassemblerModelStaxWriter staxWriter = new AppassemblerModelStaxWriter();
            staxWriter.write( writer, mergedDaemon );
        }
        catch ( IOException e )
        {
            throw new DaemonGeneratorException( "Error while writing output file: " + request.getOutputDirectory(), e );
        }
        catch ( XMLStreamException e )
        {
            throw new DaemonGeneratorException( "Error while writing output file: " + request.getOutputDirectory(), e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private Daemon createDaemon( MavenProject project, ArtifactRepositoryLayout layout, String outputFileNameMapping )
    {
        Daemon complete = new Daemon();

        complete.setClasspath( new Classpath() );

        // -----------------------------------------------------------------------
        // Add the project itself as a dependency.
        // -----------------------------------------------------------------------
        complete.getClasspath().addDependency( DependencyFactory.create( project.getArtifact(), layout,
                                                                         outputFileNameMapping ) );

        // -----------------------------------------------------------------------
        // Add all the dependencies of the project.
        // -----------------------------------------------------------------------
        for ( Iterator it = project.getRuntimeArtifacts().iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            Dependency dependency = DependencyFactory.create( artifact, layout, outputFileNameMapping );

            complete.getClasspath().addDependency( dependency );
        }

        return complete;
    }
}
