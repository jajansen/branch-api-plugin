/*
 * The MIT License
 *
 * Copyright 2015 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.branch;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.TaskListener;
import java.io.IOException;
import java.net.URL;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.actions.ChangeRequestAction;

/**
 * Defines the environment variable {@code BRANCH_NAME} for multibranch builds.
 * Also defines {@code CHANGE_*} variables when {@link ChangeRequestAction} is present.
 */
@Extension
public class BranchNameContributor extends EnvironmentContributor {

    /** {@inheritDoc} */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void buildEnvironmentFor(Job j, EnvVars envs, TaskListener listener) throws IOException, InterruptedException {
        ItemGroup parent = j.getParent();
        if (parent instanceof MultiBranchProject) {
            BranchProjectFactory projectFactory = ((MultiBranchProject) parent).getProjectFactory();
            if (projectFactory.isProject(j)) {
                SCMHead head = projectFactory.getBranch(j).getHead();
                // Note: not using Branch.name, since in the future that could be something different
                // than SCMHead.name, which is what we really want here.
                envs.put("BRANCH_NAME", head.getName());
                ChangeRequestAction cr = head.getAction(ChangeRequestAction.class);
                if (cr != null) {
                    envs.putIfNotNull("CHANGE_ID", cr.getId());
                    URL u = cr.getURL();
                    if (u != null) {
                        envs.put("CHANGE_URL", u.toString());
                    }
                    envs.putIfNotNull("CHANGE_TITLE", cr.getTitle());
                    envs.putIfNotNull("CHANGE_AUTHOR", cr.getAuthor());
                    envs.putIfNotNull("CHANGE_AUTHOR_DISPLAY_NAME", cr.getAuthorDisplayName());
                    envs.putIfNotNull("CHANGE_AUTHOR_EMAIL", cr.getAuthorEmail());
                    SCMHead target = cr.getTarget();
                    if (target != null) {
                        envs.put("CHANGE_TARGET", target.getName());
                    }
                }
            }
        }
    }

}
