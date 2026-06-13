import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { DocumentService } from '../../services/document.service';
import { ChatMessage, ChatResponse } from '../../models/chat.model';
import { Document } from '../../models/document.model';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, AfterViewChecked {
  @ViewChild('chatContainer') chatContainer!: ElementRef;
  @ViewChild('messageInput') messageInput!: ElementRef;

  /** State */
  documents: Document[] = [];
  selectedDocument: Document | null = null;
  messages: ChatMessage[] = [];
  currentMessage = '';
  sessionId = '';
  isLoading = false;
  isLoadingDocs = true;
  lastResponse: ChatResponse | null = null;
  private shouldScroll = false;

  constructor(
    private chatService: ChatService,
    private documentService: DocumentService
  ) {}

  ngOnInit(): void {
    this.sessionId = this.chatService.generateSessionId();
    this.loadDocuments();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  /** Load all available documents */
  loadDocuments(): void {
    this.isLoadingDocs = true;
    this.documentService.getAllDocuments().subscribe({
      next: (docs) => {
        this.documents = docs;
        this.isLoadingDocs = false;
        if (docs.length > 0 && !this.selectedDocument) {
          this.selectDocument(docs[0]);
        }
      },
      error: () => {
        this.isLoadingDocs = false;
      }
    });
  }

  /** Select a document to chat about */
  selectDocument(doc: Document): void {
    this.selectedDocument = doc;
    // Start a new session for each document
    this.sessionId = this.chatService.generateSessionId();
    this.messages = [];
    this.lastResponse = null;
  }

  /** Send a question */
  sendMessage(): void {
    if (!this.currentMessage.trim() || !this.selectedDocument || this.isLoading) return;

    const question = this.currentMessage.trim();
    this.currentMessage = '';
    this.isLoading = true;

    // Add user message to display immediately
    const tempMessage: ChatMessage = {
      id: Date.now(),
      sessionId: this.sessionId,
      question: question,
      answer: '',
      confidenceScore: 0,
      documentId: this.selectedDocument.id,
      createdAt: new Date().toISOString()
    };
    this.messages.push(tempMessage);
    this.shouldScroll = true;

    this.chatService.askQuestion({
      sessionId: this.sessionId,
      documentId: this.selectedDocument.id,
      question: question
    }).subscribe({
      next: (response) => {
        // Update the last message with the AI response
        const lastMsg = this.messages[this.messages.length - 1];
        lastMsg.answer = response.answer;
        lastMsg.confidenceScore = response.confidenceScore;
        this.lastResponse = response;
        this.isLoading = false;
        this.shouldScroll = true;
      },
      error: (err) => {
        const lastMsg = this.messages[this.messages.length - 1];
        lastMsg.answer = 'Sorry, an error occurred while processing your question. Please try again.';
        lastMsg.confidenceScore = 0;
        this.isLoading = false;
        this.shouldScroll = true;
      }
    });
  }

  /** Handle Enter key press */
  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  /** Get confidence level */
  getConfidenceLevel(score: number): string {
    return this.chatService.getConfidenceLevel(score);
  }

  /** Format confidence score */
  formatConfidence(score: number): string {
    return this.chatService.formatConfidence(score);
  }

  /** Format file size */
  formatFileSize(bytes: number): string {
    return this.documentService.formatFileSize(bytes);
  }

  /** Scroll chat to bottom */
  private scrollToBottom(): void {
    try {
      const container = this.chatContainer?.nativeElement;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    } catch (e) {}
  }

  /** Start a new chat session */
  newSession(): void {
    this.sessionId = this.chatService.generateSessionId();
    this.messages = [];
    this.lastResponse = null;
  }
}
