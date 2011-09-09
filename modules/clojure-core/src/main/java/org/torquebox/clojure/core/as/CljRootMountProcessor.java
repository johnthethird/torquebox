/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.clojure.core.as;

import java.io.Closeable;
import java.io.IOException;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;

/**
 * Handle mounting .clj files and marking them as a DEPLOYMENT_ROOT
 * 
 */
public class CljRootMountProcessor implements DeploymentUnitProcessor {
    
    private Closeable knobCloseable;

    public CljRootMountProcessor() {
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        log.info( "TOBY: in CljRootMountProcessor" );
        if (deploymentUnit.getAttachment( Attachments.DEPLOYMENT_ROOT ) != null) {
            return;
        }

        if (!deploymentUnit.getName().endsWith( ".clj" )) {
            return;
        }

        VirtualFile root = deploymentUnit.getAttachment( Attachments.DEPLOYMENT_CONTENTS );

        String deploymentName = deploymentUnit.getName();
        VirtualFile realRoot = root.getChild( deploymentName );
        try {
            knobCloseable = VFS.mountReal( root.getPhysicalFile(), realRoot );
            MountHandle handle = new MountHandle( knobCloseable );
            ResourceRoot expandedResourceRoot = new ResourceRoot( realRoot, handle );
            deploymentUnit.putAttachment( Attachments.DEPLOYMENT_ROOT, expandedResourceRoot );
            deploymentUnit.putAttachment( Attachments.MODULE_SPECIFICATION, new ModuleSpecification() );
            
            //hack
//            deploymentUnit.putAttachment( Attachments.DEPLOYMENT_ROOT, new ResourceRoot( VFS.getChild( "/Users/tobias/w/test-apps/ring/basic-ring/" ), null ) );
//            ClojureApplicationMetaData appMetaData = new ClojureApplicationMetaData( "basic-ring" );
//            deploymentUnit.putAttachment( ClojureApplicationMetaData.ATTACHMENT_KEY, appMetaData );
//            RingApplicationMetaData ringMetaData = new RingApplicationMetaData();
//            deploymentUnit.putAttachment( RingApplicationMetaData.ATTACHMENT_KEY, ringMetaData );
            
        } catch (IOException e) {
            throw new DeploymentUnitProcessingException( e );
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        VFSUtils.safeClose( getKnobCloseable() );
    }
    
    // Exposed for testing undeploy method
    protected Closeable getKnobCloseable() {
        return knobCloseable;
    }
    static final Logger log = Logger.getLogger( "org.torquebox.clojure.core.as" );
}
