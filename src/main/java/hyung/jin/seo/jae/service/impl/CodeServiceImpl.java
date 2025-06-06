package hyung.jin.seo.jae.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hyung.jin.seo.jae.dto.BranchDTO;
import hyung.jin.seo.jae.dto.DayScheduleDTO;
import hyung.jin.seo.jae.dto.GradeDTO;
import hyung.jin.seo.jae.dto.SimpleBasketDTO;
import hyung.jin.seo.jae.dto.StateDTO;
import hyung.jin.seo.jae.dto.SubjectDTO;
import hyung.jin.seo.jae.model.Branch;
import hyung.jin.seo.jae.model.DaySchedule;
import hyung.jin.seo.jae.model.Grade;
import hyung.jin.seo.jae.model.PracticeType;
import hyung.jin.seo.jae.model.State;
import hyung.jin.seo.jae.model.Subject;
import hyung.jin.seo.jae.model.TestType;
import hyung.jin.seo.jae.repository.BranchRepository;
import hyung.jin.seo.jae.repository.DayScheduleRepository;
import hyung.jin.seo.jae.repository.GradeRepository;
import hyung.jin.seo.jae.repository.PracticeTypeRepository;
import hyung.jin.seo.jae.repository.StateRepository;
import hyung.jin.seo.jae.repository.SubjectRepository;
import hyung.jin.seo.jae.repository.TestTypeRepository;
import hyung.jin.seo.jae.service.CodeService;

@Service
public class CodeServiceImpl implements CodeService {

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private GradeRepository gradeRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private PracticeTypeRepository practiceTypeRepository;

	@Autowired
	private TestTypeRepository testTypeRepository;

	@Autowired
	private DayScheduleRepository dayScheduleRepository;

	@Override
	public List<StateDTO> allStates() {
		List<StateDTO> dtos = new ArrayList<>();
		try{
			List<State> states = stateRepository.findAll();
			for(State state : states){
				StateDTO dto = new StateDTO(state);
				dtos.add(dto);
			}
		}catch(Exception e){
			System.out.println("No state found");
		}
		return dtos;
	}

	@Override
	public List<SimpleBasketDTO> loadState() {
		List<Object[]> objects = new ArrayList<>();
		try{
			objects = stateRepository.loadState();
		}catch(Exception e){
			System.out.println("No state found");
		}
		List<SimpleBasketDTO> dtos = new ArrayList<>();
		for(Object[] object : objects){
			SimpleBasketDTO dto = new SimpleBasketDTO(object);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<BranchDTO> allBranches() {
		List<BranchDTO> dtos = new ArrayList<>();
		try{
			dtos = branchRepository.getAllBranches();
		}catch(Exception e){
			System.out.println("No branch found");
		}
		return dtos;
	}

	@Override
	public List<BranchDTO> searchBranchByState(String state) {
		List<BranchDTO> dtos = new ArrayList<>();
		try{
			dtos = branchRepository.searchBranchByState(Long.parseLong(state));
		}catch(Exception e){
			System.out.println("No branch found");
		}
		return dtos;
	}

	@Override
	public List<SimpleBasketDTO> loadBranch() {
		List<Object[]> objects = new ArrayList<>();
		try{
			objects = branchRepository.loadBranch();
		}catch(Exception e){
			System.out.println("No branch found");
		}
		List<SimpleBasketDTO> dtos = new ArrayList<>();
		for(Object[] object : objects){
			SimpleBasketDTO dto = new SimpleBasketDTO(object);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	@Transactional
	public Branch addBranch(Branch branch) {
		Branch bran = branchRepository.save(branch);
		return bran;
	}

	@Override
	@Transactional
	public Branch updateBranch(Branch newBranch, Long id) {
		Branch bran = branchRepository.findById(id).map(branch -> {
			branch.setCode(newBranch.getCode());
			branch.setName(newBranch.getName());
			branch.setPhone(newBranch.getPhone());
			branch.setEmail(newBranch.getEmail());
			branch.setAddress(newBranch.getAddress());
			branch.setAbn(newBranch.getAbn());
			branch.setBank(newBranch.getBank());
			branch.setBsb(newBranch.getBsb());
			branch.setAccountNumber(newBranch.getAccountNumber());
			branch.setAccountName(newBranch.getAccountName());
			branch.setInfo(newBranch.getInfo());
			return branchRepository.save(branch);
		}).orElseGet(() -> {
			newBranch.setId(id);
			return branchRepository.save(newBranch);
		});
		return bran;
	}

	@Override
	@Transactional
	public void deleteBranch(Long id) {
		try {
			branchRepository.deleteById(id);
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			System.out.println("Nothing to delete");
		}
	}

	@Override
	public State getState(Long id) {
		State state = null;
		try {
			state = stateRepository.findById(id).get();
		} catch (Exception e) {
			System.out.println("No state found");
		}
		return state;
	}

	@Override
	@Transactional
	public State updateState(State newState, Long id) {
		State state = stateRepository.findById(id).map(st -> {
			st.setCode(newState.getCode());
			st.setName(newState.getName());
			return stateRepository.save(st);
		}).orElseGet(() -> {
			newState.setId(id);
			return stateRepository.save(newState);
		});
		return state;
	}

	@Override
	public BranchDTO getBranch(Long id) {
		BranchDTO dto = null;
		try{
			dto = branchRepository.findBranch(id);
		}catch(Exception e){
			System.out.println("No branch found");
		}
		return dto;
	}

	@Override
	public BranchDTO getBranch(String code) {
		BranchDTO dto = null;
		try{
			dto = branchRepository.findBranchByCode(code);
		}catch(Exception e){
			System.out.println("No branch found");
		}
		return dto;
	}


	@Override
	public BranchDTO getBranch(String state, String code) {
		BranchDTO dto = null;
		try{
			dto = branchRepository.findBranchByStateNCode(Long.parseLong(state), code);
		}catch(Exception e){
			System.out.println("No branch found");
		}
		return dto;
	}

	@Override
	public List<GradeDTO> allGrades() {
		List<GradeDTO> dtos = new ArrayList<>();
		try{
			List<Grade> grades = gradeRepository.findAll();
			for(Grade grade : grades){
				GradeDTO dto = new GradeDTO(grade);
				dtos.add(dto);
			}
		}catch(Exception e){
			System.out.println("No state found");
		}
		return dtos;
	}

	@Override
	public List<DayScheduleDTO> allDays() {
		List<DayScheduleDTO> dtos = new ArrayList<>();
		try{
			List<DaySchedule> days = dayScheduleRepository.findAll();
			for(DaySchedule day : days){
				DayScheduleDTO dto = new DayScheduleDTO(day);
				dtos.add(dto);
			}
		}catch(Exception e){
			System.out.println("No day found");
		}
		return dtos;
	}

	@Override
	public List<SimpleBasketDTO> loadGrade() {
		List<Object[]> objects = new ArrayList<>();
		try{
			objects = gradeRepository.loadGrade();
		}catch(Exception e){
			System.out.println("No state found");
		}
		List<SimpleBasketDTO> dtos = new ArrayList<>();
		for(Object[] object : objects){
			SimpleBasketDTO dto = new SimpleBasketDTO(object);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<SimpleBasketDTO> loadDay() {
		List<Object[]> objects = new ArrayList<>();
		try{
			objects = dayScheduleRepository.loadDay();
		}catch(Exception e){
			System.out.println("No day found");
		}
		List<SimpleBasketDTO> dtos = new ArrayList<>();
		for(Object[] object : objects){
			SimpleBasketDTO dto = new SimpleBasketDTO(object);
			dtos.add(dto);
		}
		return dtos;
	}


	@Override
	public Grade getGrade(Long id) {
		Grade grade = null;
		try {
			grade = gradeRepository.findById(id).get();
		} catch (Exception e) {
			System.out.println("No grade found");
		}
		return grade;	
	}

	@Override
	public String getPreviousGrade(String code) {
		String previous = "";
		try {
			previous = gradeRepository.getPrevious(code);
		} catch (Exception e) {
			System.out.println("No grade found");
		}
		return previous;
	}


	@Override
	public DaySchedule getDay(Long id) {
		DaySchedule day = null;
		try {
			day = dayScheduleRepository.findById(id).get();
		} catch (Exception e) {
			System.out.println("No grade found");
		}
		return day;	
	}

	@Override
	@Transactional
	public Grade addGrade(Grade grade) {
		Grade gr = gradeRepository.save(grade);
		return gr;
	}

	@Override
	@Transactional
	public Grade updateGrade(Grade newGrade, Long id) {
		Grade grade = gradeRepository.findById(id).map(gr -> {
			gr.setCode(newGrade.getCode());
			gr.setName(newGrade.getName());
			return gradeRepository.save(gr);
		}).orElseGet(() -> {
			newGrade.setId(id);
			return gradeRepository.save(newGrade);
		});
		return grade;
	}

	@Override
	@Transactional
	public void deleteGrade(Long id) {
		try {
			gradeRepository.deleteById(id);
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			System.out.println("Nothing to delete");
		}
	}

	@Override
	public List<SubjectDTO> allSubjects() {
		List<SubjectDTO> dtos = new ArrayList<>();
		try{
			List<Subject> subs = subjectRepository.findAll();
			for(Subject sub : subs){
				SubjectDTO dto = new SubjectDTO(sub);
				dtos.add(dto);
			}
		}catch(Exception e){
			System.out.println("No Subject found");
		}
		return dtos;
	}

	@Override
	public Subject getSubject(Long id) {
		Subject subject = null;
		try{
			subject = subjectRepository.findById(id).get();
		}catch(Exception e){
			System.out.println("No Subject found");
		}
		return subject;
	}

	@Override
	public List<SimpleBasketDTO> loadSubject() {
		List<Object[]> objects = new ArrayList<>();
		try{
			objects = subjectRepository.loadSubject();
		}catch(Exception e){
			System.out.println("No Subject found");
		}
		List<SimpleBasketDTO> dtos = new ArrayList<>();
		for(Object[] object : objects){
			SimpleBasketDTO dto = new SimpleBasketDTO(object);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public PracticeType getPracticeType(Long id) {
		PracticeType type = null;
		try{
			type = practiceTypeRepository.findById(id).get();
		}catch(Exception e){
			System.out.println("No PracticeType found");
		}
		return type;
	}

	@Override
	public List<PracticeType> getPracticeTypes(int group) {
		List<PracticeType> types = new ArrayList<>();
		try{
			types = practiceTypeRepository.findByPracticeGroup(group);
		}catch(Exception e){
			System.out.println("No ProjectType found");
		}
		return types;
	}

	@Override
	public TestType getTestType(Long id) {
		TestType type = null;
		try{
			type = testTypeRepository.findById(id).get();
		}catch(Exception e){
			System.out.println("No TestType found");
		}
		return type;
	}

	@Override
	public List<TestType> getTestTypes(int group) {
		List<TestType> types = new ArrayList<>();
		try{
			types = testTypeRepository.findByTestGroup(group);
		}catch(Exception e){
			System.out.println("No TestType found");
		}
		return types;
	}

	@Override
	public List<SimpleBasketDTO> loadPracticeType() {
		List<Object[]> objects = new ArrayList<>();
		try{
			objects = practiceTypeRepository.loadPracticeType();
		}catch(Exception e){
			System.out.println("No PracticeType found");
		}
		List<SimpleBasketDTO> dtos = new ArrayList<>();
		for(Object[] object : objects){
			SimpleBasketDTO dto = new SimpleBasketDTO(object);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<SimpleBasketDTO> loadTestType() {
		List<Object[]> objects = new ArrayList<>();
		try{
			objects = testTypeRepository.loadTestType();
		}catch(Exception e){
			System.out.println("No PracticeType found");
		}
		List<SimpleBasketDTO> dtos = new ArrayList<>();
		for(Object[] object : objects){
			SimpleBasketDTO dto = new SimpleBasketDTO(object);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public SimpleBasketDTO getBranchInfo(String code) {
		SimpleBasketDTO dto = new SimpleBasketDTO();
		try{
			dto = branchRepository.getBranchInfo(code);
		}catch(Exception e){
			System.out.println("No state found");
		}
		return dto;
	}

	@Override
	public String getBranchEmail(String code) {
		String email = "";
		Optional<String> emailOptional = branchRepository.getEmail(code);
		if (emailOptional.isPresent()) {
			email = emailOptional.get();
			// Use the email
		} 
		return email;
	}

	@Override
	public String getPracticeTypeName(Long id) {
		String name = "";
		try{
			name = practiceTypeRepository.getNameById(id);
		}catch(Exception e){
			System.out.println("No PracticeType found");
		}
		return name;
	}

	@Override
	public String getTestTypeName(Long id) {
		String name = "";
		try{
			name = testTypeRepository.getNameById(id);
		}catch(Exception e){
			System.out.println("No TestType found");
		}
		return name;
	}

}
