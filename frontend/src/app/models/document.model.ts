/**
 * Represents an uploaded PDF document.
 */
export interface Document {
  id: number;
  filename: string;
  uploadDate: string;
  filePath: string;
  pageCount: number;
  fileSize: number;
}

/**
 * Response from the document upload endpoint.
 */
export interface UploadResponse {
  id: number;
  filename: string;
  uploadDate: string;
  pageCount: number;
  fileSize: number;
  message: string;
}
