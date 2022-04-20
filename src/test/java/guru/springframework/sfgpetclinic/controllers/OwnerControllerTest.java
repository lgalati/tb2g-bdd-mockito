package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.fauxspring.BindingResult;
import guru.springframework.sfgpetclinic.fauxspring.Model;
import guru.springframework.sfgpetclinic.model.Owner;
import guru.springframework.sfgpetclinic.services.OwnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    private static final String OWNERS_CREATE_OR_UPDATE_OWNER_FORM = "owners/createOrUpdateOwnerForm";
    private static final String REDIRECT_OWNERS_5 = "redirect:/owners/5";

    @Mock
    OwnerService ownerService;

    @InjectMocks
    OwnerController controller;

    @Mock
    BindingResult bindingResult;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    @BeforeEach
    void setUp() {

        given(ownerService.findAllByLastNameLike(stringArgumentCaptor.capture()))
                .willAnswer(invocation -> {
                    List<Owner> owners = new ArrayList<>();

                    String name = invocation.getArgument(0);

                    if (name.equals("%Buck%"))  {
                        owners.add(new Owner(1l, "Joe", "Buck"));
                        return owners;
                    } else if (name.equals("%DontFindMe%")) {
                        return owners;
                    } else if (name.equals("%FindMe%")) {
                        owners.add(new Owner(1l, "Joe", "Buck"));
                        owners.add(new Owner(1l, "Joe2", "Buck2"));
                        return owners;
                    }

                    throw new RuntimeException("Invalid Argument");
                });
    }

    @Test
    void processFindFormWildcardStringFound() {
        //given
        Owner owner = new Owner(1l, "Joe", "FindMe");

        //when
        String viewName = controller.processFindForm(owner, bindingResult, Mockito.mock(Model.class));

        //then
        assertThat(stringArgumentCaptor.getValue()).isEqualToIgnoringCase("%FindMe%");
        assertThat("owners/ownersList").isEqualToIgnoringCase(viewName);
    }

    @Test
    void processFindFormWildcardStringAnnotation() {
        //given
        Owner owner = new Owner(1l, "Joe", "Buck");

        //when
        String viewName = controller.processFindForm(owner, bindingResult, null);

        //then
        assertThat(stringArgumentCaptor.getValue()).isEqualToIgnoringCase("%Buck%");
        assertThat("redirect:/owners/1").isEqualToIgnoringCase(viewName);
    }

    @Test
    void processFindFormWildcardStringNotFound() {
        //given
        Owner owner = new Owner(1l, "Joe", "DontFindMe");

        //when
        String viewName = controller.processFindForm(owner, bindingResult, null);

        //then
        assertThat(stringArgumentCaptor.getValue()).isEqualToIgnoringCase("%DontFindMe%");
        assertThat("owners/findOwners").isEqualToIgnoringCase(viewName);
    }

    @Test
    void processCreationFormHasErrors() {
        //given
        Owner owner = new Owner(1l, "Jim", "Smith");
        given(bindingResult.hasErrors()).willReturn(true);
        //when
        String viewName = controller.processCreationForm(owner, bindingResult);

        assertThat(viewName).isEqualToIgnoringCase(OWNERS_CREATE_OR_UPDATE_OWNER_FORM);
        then(ownerService).shouldHaveZeroInteractions();
    }

    @Test
    void processCreationFormNoErrors() {
        //given
        Owner owner = new Owner(5l, "Jim", "Smith");
        given(bindingResult.hasErrors()).willReturn(Boolean.FALSE);
        given(ownerService.save(any(Owner.class))).willReturn(owner);

        //when
        String viewName = controller.processCreationForm(owner, bindingResult);

        assertThat(viewName).isEqualToIgnoringCase(REDIRECT_OWNERS_5);
        then(ownerService).should().save(any(Owner.class));
    }
}