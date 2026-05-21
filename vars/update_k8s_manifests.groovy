#!/usr/bin/env groovy

/**
 * Update Kubernetes manifests with new image tags for mhanzala repositories
 */
def call(Map config = [:]) {
    def imageTag = config.imageTag ?: error("Image tag is required")
    def manifestsPath = config.manifestsPath ?: 'kubernetes'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitUserName = config.gitUserName ?: 'Hanzala-Israr'
    def gitUserEmail = config.gitUserEmail ?: 'hanzala.coder@gmail.com'
    
    echo "Updating Kubernetes manifests with image tag: ${imageTag}"
    
    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {
        // Configure Git using your credentials configuration
        sh """
            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"
        """
        
        // Update deployment manifests with new image tags
        sh """
            # Update main application deployment with your custom image namespace
            sed -i "s|image: mhanzala/easyshop-app:.*|image: mhanzala/easyshop-app:${imageTag}|g" ${manifestsPath}/08-easyshop-deployment.yaml
            
            # Update migration job if it exists with your custom migration namespace
            if [ -f "${manifestsPath}/12-migration-job.yaml" ]; then
                sed -i "s|image: mhanzala/easyshop-migration:.*|image: mhanzala/easyshop-migration:${imageTag}|g" ${manifestsPath}/12-migration-job.yaml
            fi
            
            # Ensure ingress is using the correct domain
            if [ -f "${manifestsPath}/10-ingress.yaml" ]; then
                sed -i "s|host: .*|host: easyshop.51.20.253.89.sslip.io|g" ${manifestsPath}/10-ingress.yaml
            fi
           
            
            # Check for changes
            if git diff --quiet; then
                echo "No changes to commit"
            else
                # Commit and push changes
                git add ${manifestsPath}/*.yaml
                git commit -m "Update image tags to ${imageTag} and ensure correct domain [ci skip]"
                
                # Dynamic branch detection fallback (safeguards against missing env vars)
                CURRENT_BRANCH=\${GIT_BRANCH:-"main"}
                # Clean prefix strings if Jenkins exposes it as 'origin/main'
                CURRENT_BRANCH=\${CURRENT_BRANCH##*/}
                
                # FIX: Changed target from LondheShubham153 to your own personal repository endpoint
                git remote set-url origin https://\${GIT_USERNAME}:\${GIT_PASSWORD}@github.com/Hanzala-Israr/full-stack-easyshop-kubernetes-devops.git
                git push origin HEAD:\${CURRENT_BRANCH}
            fi
        """
    }
}
