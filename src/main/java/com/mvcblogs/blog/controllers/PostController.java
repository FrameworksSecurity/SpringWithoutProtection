package com.mvcblogs.blog.controllers;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.management.Query;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.expression.Dates;

import com.mvcblogs.blog.models.PostRepository;
import com.mvcblogs.blog.models.Post;
import com.mvcblogs.blog.models.CommentRepository;
import com.mvcblogs.blog.models.Comment;

@Controller
public class PostController {	
	@Autowired
	private PostRepository postRepository;
	
	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	DataSource datasource;


	@GetMapping("/posts")
	public String list(Model model) {
		model.addAttribute("title", "Posts");
		model.addAttribute("description", "List of posts");
		model.addAttribute("posts", postRepository.findAll());
		if (!model.containsAttribute("post")) {
			model.addAttribute("post", new Post());
		}
		return "post/list";
	}
	
	@GetMapping("/posts/{postid}")
	public String show(Model model, @PathVariable("postid") Long postId) {
		Optional<Post> postData = postRepository.findById(postId);
		if (postData.isPresent()) {
			Post post = postData.get();
			model.addAttribute("title", post.getTitle());
			model.addAttribute("description", post.getDescription());
			model.addAttribute("post", post);
			if (!model.containsAttribute("comment")) {
				model.addAttribute("comment", new Comment());
			}
			return "post/show";
		}
		return "redirect:/posts";
	}
	
	@PostMapping("/posts/save")
	public String save(Model model, @Valid Post post, BindingResult result, RedirectAttributes attrs) {
		String created_atFormatted = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Calendar.getInstance().getTime());
		String updated_atFormatted = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Calendar.getInstance().getTime());
		String sql = "INSERT INTO post (id, title, created_at, updated_at, description) VALUES ("
		+post.getId()+","
		+"'"+post.getTitle()+"',"
		+"'"+created_atFormatted+"',"
		+"'"+updated_atFormatted+"',"
		+"'"+post.getDescription()+"'"
		+");";

		try{
			Connection c = datasource.getConnection();
			c.createStatement().executeUpdate(sql);
		}catch (Exception e){
			System.out.println(e.getMessage());
		}

		return "redirect:/posts";
	}
	
	@PostMapping("/posts/saveComment")
	public String saveComment(@RequestParam("post_id") Long postId, Model model, @Valid Comment comment, BindingResult result, RedirectAttributes attrs) {
		if (result.hasErrors()) {
			attrs.addFlashAttribute("org.springframework.validation.BindingResult.comment", result);
		    attrs.addFlashAttribute("comment", comment);
			return "redirect:/posts/"+postId+"/";
		}else {
			Optional<Post> postData = postRepository.findById(postId);
			if (postData.isPresent()) {
				Post post = postData.get();
				comment.setPost(post);
				commentRepository.save(comment);
			}
			return "redirect:/posts/"+postId+"/";
		}
	}
	
	@PostMapping("/posts/deleteComment/{commentid}")
	public String deleteComment(@PathVariable("commentid") Long commentId, @RequestParam("post_id") Long postId) {
		commentRepository.deleteById(commentId);
		return "redirect:/posts/"+postId+"/";
	}
	
}
