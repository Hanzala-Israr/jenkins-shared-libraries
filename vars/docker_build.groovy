def call(Map config = [:]) {
    def imageName = config.imageName ?: error("imageName parameter is required")
    def imageTag = config.imageTag ?: error("imageTag parameter is required")
    def dockerfile = config.dockerfile ?: 'Dockerfile'
    def context = config.context ?: '.'

    echo "Building Docker image: ${imageName}:${imageTag} using ${dockerfile}"
    
    // FIX: Added --security-opt seccomp=unconfined to bypass Ubuntu 24.04 kernel blocks
    sh "docker build --security-opt seccomp=unconfined -t ${imageName}:${imageTag} -t ${imageName}:latest -f ${dockerfile} ${context}"
}
