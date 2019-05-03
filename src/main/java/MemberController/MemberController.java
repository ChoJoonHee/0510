package MemberController;

import javax.servlet.http.HttpServletRequest;

import org.joonhee.book.chap11.DaoException;
import org.joonhee.book.chap11.Member;
import org.joonhee.book.chap11.MemberDao;
import org.joonhee.book.chap11.MemberDaoImpl;
import org.joonhee.web.common.Controller;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joonhee.web.common.Controller;
import org.joonhee.web.common.RequestMapping;
import org.joonhee.web.common.RequestMapping.RequestMethod;


@Controller

public class MemberController {
	
	Logger logger = LogManager.getLogger();

	MemberDao memberDao = null;

	public MemberController() throws Exception {
		memberDao = new MemberDaoImpl();
	}

	@RequestMapping(value = "/app/register/step2", method = RequestMethod.POST)
	public void step2(HttpServletRequest request,
			HttpServletResponse response) {
		String agree = request.getParameter("agree");

		if (!"true".equals(agree)) {// 동의하지 않으면 step1으로 돌아감
			logger.debug("약관에 동의하지 않았습니다.");
			forward("/WEB-INF/jsp/register/step1.jsp", request, response);
		} else { // 동의하면 step2로 forward
			forward("/WEB-INF/jsp/register/step2.jsp", request, response);
		}
	}

	@RequestMapping(value = "/app/register/step3", method = RequestMethod.POST)
	public void step3(HttpServletRequest request,
			HttpServletResponse response) {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String name = request.getParameter("name");

		// 회원정보 저장
		Member member = new Member(email, password, name);
		try {
			memberDao.insert(member);
			logger.debug("회원 정보를 저장했습니다. {}", member);
			forward("/WEB-INF/jsp/register/step3.jsp", request, response);
		} catch (DaoException e) {
			logger.error(e.getMessage());
			forward("/WEB-INF/jsp/register/step2.jsp", request, response);
		}
	}

	@RequestMapping(value = "/app/members", method = RequestMethod.GET)
	public void members(HttpServletRequest request,
			HttpServletResponse response) {

		String pageStr = request.getParameter("page");
		int page = 1;
		if (pageStr != null) {
			try { // 가져올 페이지 수
				page = Integer.parseInt(request.getParameter("page"));
			} catch (NumberFormatException e) {
				logger.error(e.getMessage());
			}
		}

		// 페이지 당 가져오는 행의 수
		final int COUNT = 100;
		// 시작점
		int offset = (page - 1) * COUNT;

		List<Member> memberList = memberDao.selectAll(offset, COUNT);

		int totalCount = memberDao.countAll();

		request.setAttribute("totalCount", totalCount);
		request.setAttribute("members", memberList);
		forward("/WEB-INF/jsp/members.jsp", request, response);
	}

	/**
	 * request forward
	 */
	private void forward(String url, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			request.getRequestDispatcher(url).forward(request, response);
		} catch (ServletException | IOException e) {
			logger.error(e.getMessage());
		}
	}
}