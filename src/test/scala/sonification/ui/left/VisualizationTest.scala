package sonification.ui.left

object VisualizationTest extends App {
  // constants
  val width = 1000
  val itemWidth = 10
  val positions = 0d to 1 by 0.05

  val pagesOptions = Seq(1, 2)

  // helper variables
  val effectiveWidthOnOnePage = width - itemWidth

  def calcPage(pages: Int, position: Double): Int = {
    1
  }

  def calcX(width: Int, pages: Int, position: Double): Int = Math.round(width * position + itemWidth / 2).toInt

  for (pages <- pagesOptions) {
    println(s"Pages $pages")
    for (position <- positions) {
      val efffectiveAvailableWidth = effectiveWidthOnOnePage * pages
      val x = calcX(efffectiveAvailableWidth, pages, position)
      val page = calcPage(pages, position)
      println(f"Position: $position%.2f x: $x%5d page: $page%5d")
    }
  }
}