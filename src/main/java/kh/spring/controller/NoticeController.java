package kh.spring.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;

import kh.spring.dto.NoticeDTO;
import kh.spring.service.NoticeService;

@RequestMapping("/notice/")
@Controller
public class NoticeController {
	
	private final NoticeService service;
	
	public NoticeController(NoticeService service) {
		this.service = service;
	}
	
	@RequestMapping("toNotice")
	public String notice(Model model) throws Exception {
		List<NoticeDTO> notices = service.selectAll();
		model.addAttribute("notices", notices);
	    return "/notice/noticeList";
	}
	
	@RequestMapping("detail")
	public String selectById(int notice_id, int member_id, Model model) {
		NoticeDTO dto = service.selectById(notice_id);
		int viewCount = service.updateViewCount(notice_id);
		NoticeDTO selectUpDown = service.selectUpDown(notice_id);
		String username = service.selectUsername(member_id);
		model.addAttribute("dto", dto);
		model.addAttribute("username", username);
		model.addAttribute("upDown", selectUpDown);
		return "/notice/noticeDetail";
	}
		
	@RequestMapping("toWrite")
	public String toWrite() {
		return "/notice/noticeWrite";
	}
	
	@RequestMapping("insert")
	public String insert(NoticeDTO dto) {
		int result = service.insert(dto); // 게시판에 작성된 내용을 DB에 저장하는 부분
		return "redirect:/notice/toNotice";
	}
	
	@RequestMapping(value="/uploadSummernoteImageFile", produces = "application/json; charset=utf8")
	@ResponseBody
	public String uploadSummernoteImageFile(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request )  {
		
		JsonObject jsonObject = new JsonObject();
		
        
		String fileRoot = "C:\\summernoteImage\\"; // 외부경로로 저장을 희망할때.
				
		// 내부경로로 저장
//		String contextRoot = new HttpServletRequestWrapper(request).getRealPath("/");
//		System.out.println(contextRoot);
//		String fileRoot = contextRoot+"resources/fileupload/";
		
		String originalFileName = multipartFile.getOriginalFilename();	//오리지날 파일명
		String extension = originalFileName.substring(originalFileName.lastIndexOf("."));	//파일 확장자
		String savedFileName = UUID.randomUUID() + extension;	//저장될 파일 명
		
		File targetFile = new File(fileRoot + savedFileName);	
		try {
			InputStream fileStream = multipartFile.getInputStream();
			FileUtils.copyInputStreamToFile(fileStream, targetFile);	//파일 저장
			jsonObject.addProperty("url", "/summernoteImage/"+savedFileName); // contextroot + resources + 저장할 내부 폴더명
			jsonObject.addProperty("responseCode", "success");
				
		} catch (IOException e) {
			FileUtils.deleteQuietly(targetFile);	//저장된 파일 삭제
			jsonObject.addProperty("responseCode", "error");
			e.printStackTrace();
		}
		String a = jsonObject.toString();
		return a;
	}
	
	@RequestMapping("delete")
	public String delete(int notice_id) {
		int result = service.delete(notice_id); // 게시판에 작성된 내용을 DB에 저장하는 부분
		return "redirect:/notice/toNotice";
	}
	
	@RequestMapping("toUpdate")
	public String toUpdate(int notice_id, Model model) {
		NoticeDTO dto = service.selectById(notice_id);
		model.addAttribute("dto", dto);
		return "/notice/noticeUpdate";
	}
	
	@RequestMapping("update")
	public String update(NoticeDTO dto, Model model) {
		int result = service.update(dto); // 게시판에 작성된 내용을 DB에 저장하는 부분
		return "redirect:/notice/detail?notice_id="+dto.getNotice_id()+"&member_id="+dto.getMember_id();
	}
	
	@ExceptionHandler(Exception.class)
	public String exceptionHandler(Exception e) {
		e.printStackTrace();
		return "redirect:/";
	}

}
