import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected title = 'ArtemisDocs AI';

  /** Navigation items for the sidebar */
  navItems = [
    { path: '/chat', label: 'Chat', icon: 'chat', description: 'Ask questions' },
    { path: '/upload', label: 'Documents', icon: 'upload', description: 'Upload PDFs' },
    { path: '/dashboard', label: 'Dashboard', icon: 'dashboard', description: 'Support tickets' }
  ];
}
