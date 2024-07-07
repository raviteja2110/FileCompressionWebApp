document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('file-input');
    const fileName = document.getElementById('file-name');
    const uploadBtn = document.getElementById('upload-btn');
    const uploadForm = document.getElementById('upload-form');
    const progressContainer = document.getElementById('progress-container');
    const progressBar = document.getElementById('progress-bar').firstElementChild;
    const progressMessage = document.getElementById('progress-message');
    const downloadLink = document.getElementById('download-link');

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            fileName.textContent = `Selected file: ${fileInput.files[0].name}`;
            uploadBtn.disabled = false;
        } else {
            fileName.textContent = '';
            uploadBtn.disabled = true;
        }
    });

    uploadForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (fileInput.files.length > 0) {
            const formData = new FormData();
            formData.append('file', fileInput.files[0]);

            uploadBtn.disabled = true;
            uploadBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Uploading...';
            progressContainer.style.display = 'block';

            try {
                const response = await fetch('/', {
                    method: 'POST',
                    body: formData
                });

                const result = await response.json();
                if (response.ok) {
                    progressBar.style.width = '100%';
                    progressBar.textContent = '100%';
                    progressMessage.textContent = 'Processing...';

                    // Redirect to countdown page
                    window.location.href = `/download-countdown/${result.fileId}`;
                } else {
                    throw new Error(result.message || 'Upload failed');
                }
            } catch (error) {
                progressMessage.textContent = `Error: ${error.message}`;
            } finally {
                uploadBtn.disabled = false;
                uploadBtn.innerHTML = '<i class="fas fa-upload"></i> Upload File';
            }
        }
    });

    downloadLink.addEventListener('click', (e) => {
        e.preventDefault();
        downloadLink.disabled = true;
        window.location.href = downloadLink.href;
    });

    function updateProgress(percentage) {
        progressBar.style.width = percentage + '%';
        progressBar.textContent = percentage + '%';
    }
});
