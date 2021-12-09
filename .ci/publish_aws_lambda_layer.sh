#!/usr/bin/env bash

layer_name=elastic-apm-java

set -euo pipefail
folder="$(dirname "${0}")"

v="${1:-}"
if [[ "" == "${v}" ]]; then
    echo "usage ${0} <version> # where <version> is the current maven project version"
fi

# create a packaged zip for layer

agent_jar="${folder}/../elastic-apm-agent/target/elastic-apm-agent-${v}.jar"

if [[ ! -f "${agent_jar}" ]]; then
    echo "missing packaged agent jar file '${agent_jar}'"
    exit 1
fi

tmp_folder="$(mktemp -d)"

cp ${agent_jar} ${tmp_folder}
cat > ${tmp_folder}/elastic-apm-handler << EOF
#!/bin/bash

export JAVA_TOOL_OPTIONS="-javaagent:/opt/elastic-apm-agent-${v}.jar \${JAVA_TOOL_OPTIONS}"

export ELASTIC_APM_SERVICE_NAME="\${AWS_LAMBDA_FUNCTION_NAME}"
export ELASTIC_APM_AWS_LAMBDA_HANDLER="\${_HANDLER}"
export ELASTIC_APM_METRICS_INTERVAL="0s"
export ELASTIC_APM_CENTRAL_CONFIG="false"
export ELASTIC_APM_CLOUD_PROVIDER="none"

exec "\$@"
EOF

cd ${tmp_folder}
zip elastic-apm-java-aws-lambda-layer.zip elastic-apm-*
cd -

aws_layer_zip=${tmp_folder}/elastic-apm-java-aws-lambda-layer.zip

# publish a new version of packaged layer from zip

aws lambda publish-layer-version \
  --layer-name "${layer_name}" \
  --description "${v}" \
  --license-info "Apache-2.0" \
  --compatible-runtimes java8 java8.al2 java11 \
  --compatible-architectures x86_64 arm64 \
  --zip-file "fileb://${aws_layer_zip}"

rm -rf ${tmp_folder}
