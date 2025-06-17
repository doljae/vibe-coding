/**
 * End-to-End Tests for Comment Functionality
 * 
 * These tests verify that the comment functionality works correctly in a real browser environment.
 * They test the following features:
 * 1. Creating a new comment and seeing it appear immediately
 * 2. Creating a reply to a comment and seeing it appear immediately
 * 3. Editing a comment and seeing the changes immediately
 * 4. Deleting a comment and seeing it disappear immediately
 * 5. Deleting a comment with replies and seeing both disappear immediately
 * 
 * To run these tests, you need to have a running instance of the application
 * and a browser automation tool like Selenium or Cypress.
 */

// Example using Cypress (pseudocode)
describe('Comment Functionality', () => {
  beforeEach(() => {
    // Visit a post detail page
    cy.visit('/post-detail.html?id=some-test-post-id');
    
    // Wait for the page to load
    cy.get('#post-container').should('be.visible');
    cy.get('#comments-section').should('be.visible');
  });

  it('should create a new comment and display it immediately', () => {
    // Fill in the comment form
    cy.get('#comment-author').type('Test Author');
    cy.get('#comment-content').type('This is a test comment');
    
    // Submit the form
    cy.get('#comment-form').submit();
    
    // Verify the comment appears immediately
    cy.get('.comment').should('contain', 'This is a test comment');
    cy.get('.comment-author').should('contain', 'Test Author');
    
    // Verify the comment count is updated
    cy.get('#comments-count').should('not.contain', '0');
  });

  it('should create a reply to a comment and display it immediately', () => {
    // First create a comment if none exists
    if (cy.get('.comment').should('not.exist')) {
      cy.get('#comment-author').type('Test Author');
      cy.get('#comment-content').type('This is a test comment');
      cy.get('#comment-form').submit();
    }
    
    // Click the reply button on the first comment
    cy.get('.comment .reply-btn').first().click();
    
    // Fill in the reply form
    cy.get('#reply-author').type('Reply Author');
    cy.get('#reply-content').type('This is a test reply');
    
    // Submit the form
    cy.get('#reply-form').submit();
    
    // Verify the reply appears immediately
    cy.get('.comment-reply').should('contain', 'This is a test reply');
    cy.get('.comment-reply .comment-author').should('contain', 'Reply Author');
  });

  it('should edit a comment and display the changes immediately', () => {
    // First create a comment if none exists
    if (cy.get('.comment').should('not.exist')) {
      cy.get('#comment-author').type('Test Author');
      cy.get('#comment-content').type('This is a test comment');
      cy.get('#comment-form').submit();
    }
    
    // Store the original content for comparison
    let originalContent;
    cy.get('.comment .comment-text').first().then(($el) => {
      originalContent = $el.text();
    });
    
    // Click the edit button on the first comment
    cy.get('.comment .edit-btn').first().click();
    
    // Type in the prompt dialog
    cy.on('window:prompt', (text, defaultValue) => {
      expect(text).to.contain('수정');
      expect(defaultValue).to.equal(originalContent);
      return 'This is an edited comment';
    });
    
    // Verify the edited comment appears immediately
    cy.get('.comment .comment-text').first().should('contain', 'This is an edited comment');
  });

  it('should delete a comment and remove it immediately', () => {
    // First create a comment if none exists
    if (cy.get('.comment').should('not.exist')) {
      cy.get('#comment-author').type('Test Author');
      cy.get('#comment-content').type('This is a test comment');
      cy.get('#comment-form').submit();
    }
    
    // Count the number of comments before deletion
    let commentCount;
    cy.get('.comment').then(($el) => {
      commentCount = $el.length;
    });
    
    // Click the delete button on the first comment
    cy.get('.comment .delete-btn').first().click();
    
    // Confirm the deletion in the dialog
    cy.on('window:confirm', (text) => {
      expect(text).to.contain('삭제');
      return true;
    });
    
    // Verify the comment is removed immediately
    cy.get('.comment').then(($el) => {
      expect($el.length).to.equal(commentCount - 1);
    });
  });

  it('should delete a comment with replies and remove both immediately', () => {
    // First create a comment if none exists
    if (cy.get('.comment').should('not.exist')) {
      cy.get('#comment-author').type('Test Author');
      cy.get('#comment-content').type('This is a test comment');
      cy.get('#comment-form').submit();
    }
    
    // Then create a reply if none exists
    if (cy.get('.comment-reply').should('not.exist')) {
      cy.get('.comment .reply-btn').first().click();
      cy.get('#reply-author').type('Reply Author');
      cy.get('#reply-content').type('This is a test reply');
      cy.get('#reply-form').submit();
    }
    
    // Count the number of comments and replies before deletion
    let commentCount;
    cy.get('.comment, .comment-reply').then(($el) => {
      commentCount = $el.length;
    });
    
    // Click the delete button on the first comment (which has a reply)
    cy.get('.comment .delete-btn').first().click();
    
    // Confirm the deletion in the dialog
    cy.on('window:confirm', (text) => {
      expect(text).to.contain('삭제');
      return true;
    });
    
    // Verify the comment and its reply are removed immediately
    cy.get('.comment, .comment-reply').then(($el) => {
      expect($el.length).to.be.at.most(commentCount - 2); // At least the comment and one reply should be gone
    });
  });
});

