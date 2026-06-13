import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentService } from '../../services/document.service';
import { Document, UploadResponse } from '../../models/document.model';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.css'
})
export class UploadComponent implements OnInit {
  documents: Document[] = [];
  isUploading = false;
  isLoading = true;
  isDragOver = false;
  uploadProgress = 0;
  uploadMessage = '';
  uploadError = '';

  constructor(private documentService: DocumentService) {}

  ngOnInit(): void {
    this.loadDocuments();
  }

  /** Load all documents */
  loadDocuments(): void {
    this.isLoading = true;
    this.documentService.getAllDocuments().subscribe({
      next: (docs) => {
        this.documents = docs;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  /** Handle drag events */
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.uploadFile(files[0]);
    }
  }

  /** Handle file input change */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.uploadFile(input.files[0]);
      input.value = ''; // Reset for re-upload of same file
    }
  }

  /** Upload a single file */
  uploadFile(file: File): void {
    // Validate file type
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      this.uploadError = 'Only PDF files are allowed';
      setTimeout(() => this.uploadError = '', 4000);
      return;
    }

    // Validate file size (50MB)
    if (file.size > 50 * 1024 * 1024) {
      this.uploadError = 'File size exceeds the 50MB limit';
      setTimeout(() => this.uploadError = '', 4000);
      return;
    }

    this.isUploading = true;
    this.uploadMessage = '';
    this.uploadError = '';
    this.uploadProgress = 0;

    // Simulate progress
    const progressInterval = setInterval(() => {
      if (this.uploadProgress < 90) {
        this.uploadProgress += Math.random() * 15;
      }
    }, 200);

    this.documentService.uploadDocument(file).subscribe({
      next: (response: UploadResponse) => {
        clearInterval(progressInterval);
        this.uploadProgress = 100;
        this.uploadMessage = `"${response.filename}" uploaded successfully! (${response.pageCount} pages)`;
        this.isUploading = false;
        this.loadDocuments();
        setTimeout(() => {
          this.uploadMessage = '';
          this.uploadProgress = 0;
        }, 4000);
      },
      error: (err) => {
        clearInterval(progressInterval);
        this.uploadProgress = 0;
        this.uploadError = err.error?.message || 'Upload failed. Please try again.';
        this.isUploading = false;
        setTimeout(() => this.uploadError = '', 5000);
      }
    });
  }

  /** Delete a document */
  deleteDocument(doc: Document): void {
    if (!confirm(`Delete "${doc.filename}"? This cannot be undone.`)) return;

    this.documentService.deleteDocument(doc.id).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.id !== doc.id);
      },
      error: () => {
        this.uploadError = 'Failed to delete document';
        setTimeout(() => this.uploadError = '', 4000);
      }
    });
  }

  /** Format file size */
  formatFileSize(bytes: number): string {
    return this.documentService.formatFileSize(bytes);
  }

  /** Format date */
  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric'
    });
  }
}
