package org.kie.uberfire.social.activities.client.widgets.timeline.simple;

import java.util.List;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Legend;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.uberfire.social.activities.client.widgets.item.SimpleItemWidget;
import org.kie.uberfire.social.activities.client.widgets.item.model.SimpleItemWidgetModel;
import org.kie.uberfire.social.activities.client.widgets.timeline.simple.model.SimpleSocialTimelineWidgetModel;
import org.kie.uberfire.social.activities.model.PagedSocialQuery;
import org.kie.uberfire.social.activities.model.SocialActivitiesEvent;
import org.kie.uberfire.social.activities.model.SocialPaged;
import org.kie.uberfire.social.activities.service.SocialTimeLineRepositoryAPI;
import org.kie.uberfire.social.activities.service.SocialTypeTimelinePagedRepositoryAPI;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.VFSService;

public class SimpleSocialTimelineWidget extends Composite {

    private SimpleSocialTimelineWidgetModel model;

    @UiField
    FlowPanel title;

    @UiField
    FluidContainer itemsPanel;

    private SocialPaged socialPaged;

    public SimpleSocialTimelineWidget( SimpleSocialTimelineWidgetModel model ) {

        this.model = model;
        initWidget( uiBinder.createAndBindUi( this ) );
        title.add( new Legend( model.getTitle() ) );
        if ( model.isSocialTypeWidget() ) {
            createSociaTypelItemsWidget( model );
        } else {
            createUserTimelineItemsWidget( model );
        }

    }

    private void createUserTimelineItemsWidget( final SimpleSocialTimelineWidgetModel model ) {
        MessageBuilder.createCall( new RemoteCallback<List<SocialActivitiesEvent>>() {
            public void callback( List<SocialActivitiesEvent> events ) {
                for ( final SocialActivitiesEvent event : events ) {
                    if ( event.hasLink() ) {
                        MessageBuilder.createCall( new RemoteCallback<Path>() {
                            public void callback( Path path ) {
                                SimpleItemWidgetModel rowModel = new SimpleItemWidgetModel( model, event.getTimestamp(), event.getLinkLabel(), path, event.getAdicionalInfos() );
                                FluidRow row = SimpleItemWidget.createRow( rowModel );
                                itemsPanel.add( row );
                            }
                        }, VFSService.class ).get( event.getLinkTarget() );
                    } else {
                        SimpleItemWidgetModel rowModel = new SimpleItemWidgetModel( model, event.getTimestamp(),event.getDescription(), event.getAdicionalInfos() );
                        FluidRow row = SimpleItemWidget.createRow( rowModel );
                        itemsPanel.add( row );
                    }

                }
            }
        }, SocialTimeLineRepositoryAPI.class ).getLastUserTimeline( model.getSocialUser(), model.getPredicate() );
    }

    private void createSociaTypelItemsWidget( final SimpleSocialTimelineWidgetModel model ) {
        socialPaged = model.getSocialPaged();
        MessageBuilder.createCall( new RemoteCallback<PagedSocialQuery>() {
            public void callback( PagedSocialQuery paged ) {
                socialPaged = paged.socialPaged();
                for ( final SocialActivitiesEvent event : paged.socialEvents() ) {
                    if ( event.hasLink() ) {
                        MessageBuilder.createCall( new RemoteCallback<Path>() {
                            public void callback( Path path ) {
                                SimpleItemWidgetModel rowModel = new SimpleItemWidgetModel( model, event.getTimestamp(), event.getLinkLabel(), path, event.getAdicionalInfos() );
                                FluidRow row = SimpleItemWidget.createRow( rowModel );
                                itemsPanel.add( row );
                            }
                        }, VFSService.class ).get( event.getLinkTarget() );
                    } else {
                        SimpleItemWidgetModel rowModel = new SimpleItemWidgetModel( model, event.getTimestamp(),event.getDescription(), event.getAdicionalInfos() );
                        FluidRow row = SimpleItemWidget.createRow( rowModel );
                        itemsPanel.add( row );
                    }
                }
                setupPaginationButtons();
            }
        }, SocialTypeTimelinePagedRepositoryAPI.class ).getEventTimeline( model.getSocialEventType().name(), socialPaged );
    }

    private void setupPaginationButtons() {
        if ( socialPaged.canIGoBackward() ) {
            itemsPanel.add( moreEventsButton( "<" ) );
        }
        if ( socialPaged.canIGoForward() ) {
            itemsPanel.add( moreEventsButton( ">" ) );
        }
    }

    private Button moreEventsButton( final String text ) {

        Button button = GWT.create( Button.class );
        button.setText( text );

        button.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                itemsPanel.clear();
                if ( text.equalsIgnoreCase( "<" ) ) {
                    socialPaged.backward();
                } else {
                    socialPaged.forward();
                }
                createSociaTypelItemsWidget( model );
            }
        } );
        return button;
    }

    interface MyUiBinder extends UiBinder<Widget, SimpleSocialTimelineWidget> {

    }

    static MyUiBinder uiBinder = GWT.create( MyUiBinder.class );

}
