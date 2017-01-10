import org.denigma.threejs._
import org.scalajs.dom
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js
import scala.util.Random
import org.scalajs.dom

import scala.scalajs.js.Dynamic

trait Container3D extends SceneContainer
{

  container.style.width = width.toString
  container.style.height = height.toString
  container.style.position = "relative"

  override type RendererType =  WebGLRenderer

  protected def initRenderer() = {
    val params = Dynamic.literal(
      antialias = true,
      alpha = true
      //canvas = container
    ).asInstanceOf[ WebGLRendererParameters]
    val vr = new WebGLRenderer(params)

    vr.domElement.style.position = "absolute"
    vr.domElement.style.top	  = "0"
    vr.domElement.style.margin	  = "0"
    vr.domElement.style.padding  = "0"
    vr.setSize(width,height)
    vr
  }
  val cssScene = new Scene()

  val controls: CameraControls = new HoverControls(camera, this.container)

  container.appendChild( renderer.domElement )

  override def onEnterFrame() = {
    controls.update()
    renderer.render( scene, camera )
  }

}

trait SceneContainer{

  val container: HTMLElement

  def width:Double

  def height:Double

  type RendererType <:Renderer

  val scene = new Scene()

  def distance:Double = 2000

  lazy val renderer: RendererType = this.initRenderer()

  lazy val camera = initCamera()

  def aspectRatio = width /height

  protected def initRenderer():RendererType

  protected def initCamera() =
  {
    val camera = new PerspectiveCamera(40, this.aspectRatio, 1, 1000000)
    camera.position.z = distance
    camera
  }

  protected def onEnterFrameFunction(double: Double): Unit = {
    onEnterFrame()
    render()
  }

  def onEnterFrame():Unit = {
    renderer.render(scene, camera)
  }

  def render() =  dom.window.requestAnimationFrame(  onEnterFrameFunction _ )

}





object ThreeJsDemo {

  def activate(): Unit = {
    val el: HTMLElement = dom.document.getElementById("container").asInstanceOf[HTMLElement]
    val demo = new ExampleScene(el, 1280, 500) // scalastyle:ignore
    demo.render()
  }
}

// scalastyle:off
class ExampleScene(val container: HTMLElement, val width: Double, val height: Double) extends Container3D {
  val geometry = new BoxGeometry(350, 300, 250)

  val colors = List("green", "red", "blue", "orange", "purple", "teal")
  val colorMap = Map(colors.head -> 0xA1CF64, colors(1) -> 0xD95C5C, colors(2) -> 0x6ECFF5,
    colors(3) -> 0xF05940, colors(4) -> 0x564F8A, colors.last -> 0x00B5AD)

  def materialParams(name: String): MeshLambertMaterialParameters = js.Dynamic.literal(
    color = new Color(colorMap(name)) // wireframe = true
  ).asInstanceOf[MeshLambertMaterialParameters]

  def randColorName: String = colors(Random.nextInt(colors.size))

  var meshes = addMesh(new Vector3(0, 0, 0)) :: addMesh(new Vector3(400, 0, 200)) :: addMesh(new Vector3(-400, 0, 200)) :: Nil

  override val controls: CameraControls = new ExampleControls(camera, this.container, scene, width, height, this.meshes.head.position.clone())

  val light = new DirectionalLight(0xffffff, 2)
  light.position.set(1, 1, 1).normalize()
  scene.add(light)

  def addMesh(pos: Vector3 = new Vector3()): Mesh = {
    val material = new MeshLambertMaterial(this.materialParams(randColorName))
    val mesh: Mesh = new Mesh(geometry, material)
    mesh.name = pos.toString
    mesh.position.set(pos.x, pos.y, pos.z)
    mesh
  }

  meshes.foreach(scene.add)
}

// scalastyle: on

/**
  * Just shows that some effects are working
  * @param cam the camera control
  * @param el the html element
  * @param sc scene
  * @param center center of screen
  */
class ExampleControls(cam: Camera, el: HTMLElement, sc: Scene, width: Double, height: Double,
  center: Vector3 = new Vector3()) extends JumpCameraControls(cam, el, sc, width, height, center) {
  import org.querki.jquery._

  lazy val $el = $(el)

  override def onMouseMove(event: MouseEvent) = {
    val (offsetX, offsetY) = ($el.offset().left, $el.offset().top)
    this.onCursorMove(event.clientX - offsetX, event.clientY - offsetY, width, height)

    enter.keys.foreach {
      case m: Mesh =>
        m.material match {
          case mat: MeshLambertMaterial => mat.wireframe = true
          case _ => // do nothing
        }

      case _ => // do nothing
    }

    exit.keys.foreach {
      case m: Mesh =>
        m.material match {
          case mat: MeshLambertMaterial => mat.wireframe = false
          case _ => // do nothing
        }

      case _ => // do nothing
    }

    rotateOnMove(event)

  }

}
