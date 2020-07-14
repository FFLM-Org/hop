/*! ******************************************************************************
 *
 * Hop : The Hop Orchestration Platform
 *
 * http://www.project-hop.org
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.beam.engines.flink;

import org.apache.hop.beam.engines.BeamBasePipelineEngineTest;
import org.apache.hop.beam.util.BeamPipelineMetaUtil;
import org.apache.hop.core.variables.VariableValueDescription;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.config.IPipelineEngineRunConfiguration;
import org.apache.hop.pipeline.config.PipelineRunConfiguration;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class BeamFlinkPipelineEngineTest extends BeamBasePipelineEngineTest {

  @Test
  public void testFlinkPipelineEngine() throws Exception {

    IPipelineEngineRunConfiguration configuration = new BeamFlinkPipelineRunConfiguration("[local]", "6");
    configuration.setEnginePluginId( "BeamFlinkPipelineEngine"  );
    PipelineRunConfiguration pipelineRunConfiguration = new PipelineRunConfiguration("flink", "description",
      Arrays.asList(new VariableValueDescription("VAR1", "flink1", "description1")),
      configuration
    );
    // Save the metadata
    metadataProvider.getSerializer( PipelineRunConfiguration.class).save( pipelineRunConfiguration );

    PipelineMeta pipelineMeta = BeamPipelineMetaUtil.generateBeamInputOutputPipelineMeta( "input-process-output", "INPUT", "OUTPUT", metadataProvider );

    IPipelineEngine<PipelineMeta> engine = createAndExecutePipeline(pipelineRunConfiguration.getName(), metadataProvider, pipelineMeta);
    validateInputOutputEngineMetrics( engine );

    assertEquals("flink1", engine.getVariable( "VAR1" ));
  }

}