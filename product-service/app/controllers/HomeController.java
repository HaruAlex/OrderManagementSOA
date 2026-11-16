package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * Home Controller
 */
public class HomeController extends Controller {

    public Result index() {
        return redirect("/products/manage");
    }
}
