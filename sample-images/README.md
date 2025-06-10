# Sample Images for API Testing

This directory contains sample images for testing the image upload functionality of the Blog Service API.

## Creating Test Images

You can create simple test images using various methods:

### Method 1: Using ImageMagick (if available)
```bash
# Create a simple colored rectangle
convert -size 300x200 xc:blue sample-blue.png
convert -size 400x300 xc:red sample-red.jpg
convert -size 200x200 xc:green sample-green.png
```

### Method 2: Using Python (if available)
```python
from PIL import Image, ImageDraw, ImageFont

# Create a simple test image
img = Image.new('RGB', (400, 300), color='lightblue')
draw = ImageDraw.Draw(img)
draw.text((50, 150), "Test Image for Blog API", fill='black')
img.save('test-image.png')
```

### Method 3: Manual Creation
1. Create simple images using any image editor (Paint, GIMP, etc.)
2. Save them as PNG or JPG files
3. Keep file sizes reasonable (< 5MB) for testing

## Recommended Test Images

Create the following test images for comprehensive testing:

1. **spring-boot-architecture.png** - A diagram or screenshot (300x200px)
2. **code-example.png** - A code snippet screenshot (400x300px)
3. **test-image.jpg** - A simple colored image (200x200px)
4. **large-image.png** - A larger image for size testing (800x600px)
5. **small-image.png** - A very small image (50x50px)

## File Formats Supported

The API supports common image formats:
- PNG (.png)
- JPEG (.jpg, .jpeg)
- GIF (.gif)
- WebP (.webp)

## Usage in HTTP Scripts

Reference these images in your `.http` files:

```http
POST {{baseUrl}}/api/posts/{{postId}}/images
Content-Type: multipart/form-data; boundary=boundary

--boundary
Content-Disposition: form-data; name="image"; filename="test-image.png"
Content-Type: image/png

< ./sample-images/test-image.png
--boundary--
```

## Notes

- Keep image files small for faster testing
- Use descriptive filenames
- Test with different image formats and sizes
- Ensure images are not copyrighted if sharing the project

