#!/usr/bin/env python3
"""
Script to create sample images for API testing.
Requires Pillow: pip install Pillow
"""

import os
from PIL import Image, ImageDraw, ImageFont

def create_test_images():
    """Create sample images for API testing."""
    
    # Create sample-images directory if it doesn't exist
    os.makedirs('sample-images', exist_ok=True)
    
    # Image 1: Spring Boot Architecture diagram placeholder
    img1 = Image.new('RGB', (400, 300), color='lightblue')
    draw1 = ImageDraw.Draw(img1)
    
    # Draw a simple architecture diagram
    draw1.rectangle([50, 50, 350, 100], fill='white', outline='black', width=2)
    draw1.text((60, 70), "Spring Boot Application", fill='black')
    
    draw1.rectangle([50, 120, 150, 170], fill='lightgreen', outline='black', width=2)
    draw1.text((60, 140), "Controller", fill='black')
    
    draw1.rectangle([170, 120, 270, 170], fill='lightyellow', outline='black', width=2)
    draw1.text((180, 140), "Service", fill='black')
    
    draw1.rectangle([290, 120, 350, 170], fill='lightcoral', outline='black', width=2)
    draw1.text((300, 140), "Repository", fill='black')
    
    # Draw arrows
    draw1.line([150, 145, 170, 145], fill='black', width=2)
    draw1.line([270, 145, 290, 145], fill='black', width=2)
    
    img1.save('sample-images/spring-boot-architecture.png')
    print("Created: spring-boot-architecture.png")
    
    # Image 2: Code example screenshot placeholder
    img2 = Image.new('RGB', (500, 400), color='#2d3748')
    draw2 = ImageDraw.Draw(img2)
    
    # Simulate code with colored text
    code_lines = [
        "@RestController",
        "@RequestMapping(\"/api/posts\")",
        "public class PostController {",
        "",
        "    @GetMapping",
        "    public ResponseEntity<List<Post>> getAllPosts() {",
        "        List<Post> posts = postService.getAllPosts();",
        "        return ResponseEntity.ok(posts);",
        "    }",
        "}"
    ]
    
    y_offset = 20
    for line in code_lines:
        color = 'white'
        if line.startswith('@'):
            color = '#ffd700'  # Gold for annotations
        elif 'public' in line or 'class' in line:
            color = '#87ceeb'  # Sky blue for keywords
        elif line.strip().startswith('//'):
            color = '#90ee90'  # Light green for comments
            
        draw2.text((20, y_offset), line, fill=color)
        y_offset += 25
    
    img2.save('sample-images/code-example.png')
    print("Created: code-example.png")
    
    # Image 3: Simple test image
    img3 = Image.new('RGB', (200, 200), color='lightgreen')
    draw3 = ImageDraw.Draw(img3)
    draw3.ellipse([50, 50, 150, 150], fill='darkgreen', outline='black', width=3)
    draw3.text((85, 95), "TEST", fill='white')
    img3.save('sample-images/test-image.png')
    print("Created: test-image.png")
    
    # Image 4: Large image for size testing
    img4 = Image.new('RGB', (800, 600), color='lightcoral')
    draw4 = ImageDraw.Draw(img4)
    
    # Create a pattern
    for i in range(0, 800, 50):
        draw4.line([i, 0, i, 600], fill='white', width=1)
    for i in range(0, 600, 50):
        draw4.line([0, i, 800, i], fill='white', width=1)
    
    draw4.text((350, 290), "LARGE TEST IMAGE", fill='darkred')
    draw4.text((360, 320), "800x600 pixels", fill='darkred')
    img4.save('sample-images/large-image.png')
    print("Created: large-image.png")
    
    # Image 5: Small image
    img5 = Image.new('RGB', (50, 50), color='purple')
    draw5 = ImageDraw.Draw(img5)
    draw5.ellipse([10, 10, 40, 40], fill='yellow')
    img5.save('sample-images/small-image.png')
    print("Created: small-image.png")
    
    # Image 6: JPEG format test
    img6 = Image.new('RGB', (300, 200), color='orange')
    draw6 = ImageDraw.Draw(img6)
    draw6.rectangle([50, 50, 250, 150], fill='darkorange', outline='black', width=2)
    draw6.text((120, 95), "JPEG TEST", fill='white')
    img6.save('sample-images/test-image.jpg', 'JPEG')
    print("Created: test-image.jpg")
    
    print("\nAll test images created successfully!")
    print("You can now use these images in your HTTP API tests.")

if __name__ == "__main__":
    try:
        create_test_images()
    except ImportError:
        print("Error: Pillow library not found.")
        print("Please install it with: pip install Pillow")
        print("\nAlternatively, you can create test images manually and place them in the sample-images directory.")
    except Exception as e:
        print(f"Error creating images: {e}")
        print("You can create test images manually and place them in the sample-images directory.")

