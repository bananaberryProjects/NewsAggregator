/**
 * Strips HTML tags from a string and returns plain text.
 * - Removes all HTML tags
 * - Completely removes img tags (including their content/attributes)
 * - Truncates text to maxLength if specified
 */
export function stripHtml(html: string | null | undefined, maxLength?: number): string {
  if (!html) {
    return ''
  }

  // Create a temporary DOM element to parse the HTML
  const tmp = document.createElement('div')

  // First, remove all img tags completely (they should not appear as text)
  const htmlWithoutImages = html.replace(/<img[^>]*>/gi, '')

  // Set the cleaned HTML content
  tmp.innerHTML = htmlWithoutImages

  // Get the text content (this strips all remaining HTML tags)
  let text = tmp.textContent || tmp.innerText || ''

  // Normalize whitespace: collapse multiple spaces/newlines into single space
  text = text.replace(/\s+/g, ' ').trim()

  // Truncate if maxLength is specified and text is longer
  if (maxLength && text.length > maxLength) {
    text = text.substring(0, maxLength).trim() + '...'
  }

  return text
}
